package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.core.ui.AbstractDocActivity;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.common.Situation;
import dz.jsoftware95.queue.response.ResponseConfig;
import dz.jsoftware95.queue.response.ServiceInfo;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.common.CollectionUtil;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public class DashboardRepository extends LazyRepository<ProgressDAO> {

    private final Lazy<CoreAPI> coreAPI;
    private final StateRepository stateRepository;
    private final BuildConfiguration buildConf;
    private final RepositoryUtil repositoryUtil;
    private final AbstractUpdateHandler updateHandler;
    protected final MediatorLiveData<Selection> selection = new MediatorLiveData<>();
    protected final LiveDataWrapper<Selection.Statistics> selectionStatistics = new LiveDataWrapper<>();
    protected final Map<Integer, LiveDataWrapper<Estimation>> estimations = new HashMap<>(4);
    protected volatile Selection currentSelection = null;

    @Inject
    @AnyThread
    public DashboardRepository(@NonNull final Lazy<ProgressDAO> progressDAO,
                               @NonNull final Lazy<CoreAPI> coreAPI,
                               @NonNull final StateRepository stateRepository,
                               @NonNull final BuildConfiguration buildConf,
                               @NonNull final RepositoryUtil repositoryUtil,
                               @NonNull final AbstractUpdateHandler updateHandler) {
        super(progressDAO);
        this.coreAPI = coreAPI;
        this.stateRepository = stateRepository;
        this.buildConf = buildConf;
        this.repositoryUtil = repositoryUtil;
        this.updateHandler = updateHandler;
        newInitJob(selection, this).execute();
//        initializeIfNeeded();
    }

    @NotNull
    private static DuoJob<LiveData<Selection>, DashboardRepository> newInitJob(final MediatorLiveData<Selection> selection, final DashboardRepository repository) {
        return new DuoJob<LiveData<Selection>, DashboardRepository>(selection, repository) {
            @Override
            protected void doFromMain(@NonNull LiveData<Selection> selection, @NonNull DashboardRepository repository) {
                selection.observeForever(repository::onSelection);
            }
        };
    }

    @Override
    protected void ontInitialize() {
        ProgressDAO dao = requireDAO();
        int maxCache = AppConfig.getInstance().getRemoteInt(LongSetting.MAX_PROGRESS_CACHE_IN_DAYS);
        dao.cleanOldProgresses(GlobalUtil.getOldestAcceptedCacheTimestamp(maxCache));
        execute(newAddSelectionSourceJob(this, dao.getCurrentServiceID()));
        refresh(true);
    }

    @NotNull
    public static Job newAddSelectionSourceJob(@NonNull DashboardRepository repository, LiveData<Long> currentServiceID) {
        return new DuoJob<DashboardRepository, LiveData<Long>>(repository, currentServiceID) {
            @Override
            protected void doFromMain(@NonNull DashboardRepository repository, @NonNull LiveData<Long> source) {
                repository.selection.addSource(source, serviceID -> new UnDatabaseJob<DashboardRepository>(repository) {
                    @Override
                    protected void doFromBackground(@NonNull final DashboardRepository repository) {
                        Selection selection = repository.refreshSelection(serviceID);
                        if (repository.canAutoFetchOrRefresh(selection)) {
                            repository.postPublish(DataEvent.FETCHING_DATA);
                            repository.execute(repository.newFetchServiceInfoJob(selection, false));
                        }
                    }
                }.execute());
            }
        };
    }

    private Lazy<CoreAPI> getCoreAPI() {
        return coreAPI;
    }

    @WorkerThread
    private Selection refreshSelection(Long serviceID) {
        Selection selection = loadSelection(serviceID);
        this.selection.postValue(selection);
        return selection;
    }

    @WorkerThread
    private Selection loadSelection(@Nullable Long serviceID) {
        if (serviceID != null) {
            ServiceProgress serviceProgress = requireDAO().getServiceProgressValue(serviceID);
            if (serviceProgress != null)
                return new Selection(serviceProgress);
        }
        return null;
    }

    public LiveData<Selection> getSelection() {
        initializeIfNeeded();
        return selection;
    }

    @MainThread
    private void onSelection(Selection selection) {
        currentSelection = selection;
        selectionStatistics.setSource(Selection.getStatistics(selection));

        for (LiveDataWrapper<Estimation> dataWrapper : estimations.values())
            dataWrapper.clearSource();

        if (selection != null)
            for (Map.Entry<Integer, MutableLiveData<Progress>> progressData : selection.getProgresses().entrySet()) {
                LiveDataWrapper<Estimation> dataWrapper = estimations.get(progressData.getKey());
                if (dataWrapper == null)
                    estimations.put(progressData.getKey(), dataWrapper = new LiveDataWrapper<>());
                dataWrapper.setSource(Estimation.liveDataFrom(progressData.getValue()));
            }
    }

    public LiveData<Estimation> getEstimation(Long progressID) {
        if (progressID != null && progressID > Item.AUTO_ID) {
            LiveDataWrapper<Estimation> wrapper = estimations.get(IdentityManager.getProgressRank(progressID));
            return wrapper != null ? wrapper.getLiveData() : null;
        } else
            return null;
    }

    public void handleServiceUpdate(Long service, Map<String, String> data) {
        execute(newUpdateDataJob(this, service, data));
    }

    protected static Job newUpdateDataJob(DashboardRepository repository, @Nullable Long serviceID, Map<String, String> data) {
        return new DuoDatabaseJob<DashboardRepository, Map<String, String>>(repository, data) {
            @Override
            protected void doFromBackground(@NonNull DashboardRepository repository, @NonNull Map<String, String> data) {
                ProgressDAO dao = repository.requireDAO();

                Set<Long> progressesChanged = dao.update(serviceID, data);
                if (serviceID != null && serviceID.equals(dao.getCurrentServiceIdValue())) {
                    if (progressesChanged == null)
                        repository.refreshSelection(serviceID);
                    else
                        repository.refreshProgresses(serviceID, progressesChanged);
                }

//                dao.update(serviceID, data);
//                if (serviceID != null && serviceID.equals(dao.getCurrentServiceIdValue()))
//                    repository.refreshSelection(serviceID);
            }
        };
    }

    @WorkerThread
    private void refreshProgresses(long serviceID, @NonNull Set<Long> changedProgresses) {
        if (!changedProgresses.isEmpty()) {
            Selection currentSelection = this.currentSelection;
            if (currentSelection != null) {
                Service currentService = currentSelection.getService();
                Map<Integer, MutableLiveData<Progress>> currentProgresses = currentSelection.getProgresses();
                if (currentService.getId() == serviceID && !currentProgresses.isEmpty()) {
                    for (Long changedID : changedProgresses) {
                        int changedRank = IdentityManager.getProgressRank(changedID);
                        MutableLiveData<Progress> liveData = currentProgresses.get(changedRank);
                        if (liveData != null)
                            liveData.postValue(requireDAO().requireProgressValue(changedID));
                    }
                }
            }
        }
    }

    @Override
    @AnyThread
    public void invalidate() {
        execute(newFetchServiceInfoJob(currentSelection, true));
    }

    public void refresh(boolean feedback) {
        postPublish(DataEvent.FETCHING_DATA);
        execute(newFetchServiceInfoJob(currentSelection, feedback));
    }

    @WorkerThread
    public void refreshAndWait() throws InterruptedException {
        postPublish(DataEvent.FETCHING_DATA);
        executeAndWait(newFetchServiceInfoJob(currentSelection, false));
    }

    @AnyThread
    private Job newFetchServiceInfoJob(Selection selection, boolean feedbackEnabled) {
        if (Selection.hasService(selection))
            return fetchService(selection.getService().getId(), feedbackEnabled);
        else
            return fetchService(null, feedbackEnabled);
    }

    private boolean canAutoFetchOrRefresh(Selection selection) {
        AtomicBoolean canFetch = new AtomicBoolean();
        if (Selection.hasService(selection)) {
            long now = System.currentTimeMillis();
            long serviceID = selection.getService().getId();
            CollectionUtil.compute(selection.isKnown() ? ContextUtils.AUTO_INFO_REFRESH_CACHE : ContextUtils.AUTO_INFO_FETCH_CACHE, serviceID, (id, fetchTime) -> {
                if (fetchTime == null || (fetchTime + ContextUtils.MIN_AUTO_FETCH_DELAY < now)) {
                    canFetch.set(true);
                    return now;
                } else
                    return fetchTime;
            });
        }
        return canFetch.get();
    }

    private Job fetchService(Long serviceID, boolean feedback) {
        return new FetchServiceInfoJob(this, stateRepository, serviceID, buildConf.getServerTarget(), feedback);
    }

    public void setTicket(Integer ticket, long progress) {
        if (ticket == null) {
            final BasicActivity activity = BasicActivity.CURRENT_STARTED_ACTIVITY;
            if (activity instanceof AbstractDocActivity)
                ((AbstractDocActivity) activity).considerRatingDialog();
        }
        execute(newSetTicketJob(this, ticket, progress));
    }

    private Job newSetTicketJob(DashboardRepository repository, Integer ticket, long progress) {
        return new UnDatabaseJob<DashboardRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull DashboardRepository context) {
                if (context.requireDAO().updateTicket(ticket, progress))
                    context.refreshProgresses(IdentityManager.getServiceID(progress), Collections.singleton(progress));
            }
        };
    }

    @MainThread
    public LiveData<Selection.Statistics> getStatistics() {
        return selectionStatistics.getLiveData();
    }

    private static final class FetchServiceInfoJob extends DuoJob<DashboardRepository, StateRepository> {
        private static final String NAME = "fetch_service_info_job";

        @Nullable
        private final Long serviceID;
        private final int serverTarget;
        private final boolean feedback;
        private final boolean original;

        public FetchServiceInfoJob(DashboardRepository dashboardRepository,
                                   StateRepository stateRepository,
                                   @Nullable Long serviceID, int serverTarget, boolean feedback) {
            super(AppWorker.NETWORK, dashboardRepository, stateRepository);
            this.serviceID = serviceID;
            this.serverTarget = serverTarget;
            this.feedback = feedback;
            this.original = true;
        }

        public FetchServiceInfoJob(DashboardRepository repository,
                                   StateRepository stateRepository,
                                   FetchServiceInfoJob originalJob) {
            super(AppWorker.NETWORK, repository, stateRepository);
            this.serviceID = originalJob.serviceID;
            this.serverTarget = originalJob.serverTarget;
            this.feedback = originalJob.feedback;
            this.original = false;
            Teller.info(NAME + " cloned with feedback: " + feedback);
        }

        @Override
        protected void doFromBackground(final @NotNull DashboardRepository dashboardRepository,
                                        final @NonNull StateRepository stateRepository) throws InterruptedException {
            Call<ServiceInfo> call = null;
            Teller.info(NAME + "#doFromBackground: feedback=" + feedback);
            try {
                AppConfig appConfig = AppConfig.getInstance();
                ActivationState activationState = appConfig.getActivationState();
                Long activationKey = activationState.getKey();
                if (activationKey != null) {
                    CoreAPI coreAPI = dashboardRepository.getCoreAPI().get();
                    call = getServiceInfoCall(dashboardRepository, activationKey, coreAPI);
                    Response<ServiceInfo> response = call.execute();
                    if (response.isSuccessful()) {
                        ServiceInfo serviceInfo = response.body();
                        if (serviceInfo != null) {
                            Map<String, String> data = serviceInfo.getData();
                            dashboardRepository.updateHandler.handleUpdate(data);

                            Long expirationDate = StringUtil.parseLong(StringUtil.getString(data, ResponseConfig.EXPIRATION_DATE));
                            AppConfig.getInstance().updateActivationExpirationDate(expirationDate);

                            Integer currentSchema = AppConfig.getInstance().getSchemaVersion();
                            if (currentSchema != AppConfig.NULL_SCHEMA && serviceInfo.getSchema() > currentSchema)
                                QueueUtils.requestCacheInvalidation();
                        } else
                            Teller.logMissingInfo("null body", feedback);

                        dashboardRepository.repositoryUtil.handleResponse(serviceInfo, NAME);
                    } else
                        dashboardRepository.repositoryUtil.handleUnsuccessfulResponse(response, "could not update services list: " + QueueUtils.getUrl(call),
                                () -> original ? new FetchServiceInfoJob(dashboardRepository, stateRepository, this) : null, feedback, NAME);
                } else
                    dashboardRepository.repositoryUtil.handleActivationNeeded(() -> original ? new FetchServiceInfoJob(dashboardRepository, stateRepository, this) : null, feedback, NAME);
            } catch (Exception e) {
                dashboardRepository.repositoryUtil.handleRequestException(e, "error while updating services list: " + QueueUtils.getUrl(call), feedback);
            } finally {
                dashboardRepository.postPublish(DataEvent.DATA_FETCHED);
            }
        }

        @WorkerThread
        private Call<ServiceInfo> getServiceInfoCall(@NotNull DashboardRepository dashboardRepository, Long activationKey, CoreAPI coreAPI) throws InterruptedException {
            Situation situation = getSituation(dashboardRepository);
            if (serviceID != null)
                return coreAPI.getTokens(serviceID, activationKey, serverTarget, situation);
            else
                return coreAPI.syncWithoutService(activationKey, serverTarget, situation);
        }

        @NotNull
        @WorkerThread
        private Situation getSituation(@NotNull DashboardRepository dashboardRepository) throws InterruptedException {
            StateDAO stateDAO = dashboardRepository.stateRepository.waitForDAO(getClass().getSimpleName());
            Situation situation = QueueUtils.newSituation(stateDAO, dashboardRepository.buildConf);

            ProgressDAO progressDAO = dashboardRepository.waitForDAO(getClass().getSimpleName());
            situation.setCurrentTickets(serviceID != null ? progressDAO.getTickets(serviceID) : Collections.emptyList());
            situation.setAlarmsInfo(progressDAO.getAlarmsInfo());

            return situation;
        }

    }
}
