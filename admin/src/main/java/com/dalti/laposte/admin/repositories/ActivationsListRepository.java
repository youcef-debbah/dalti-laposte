package com.dalti.laposte.admin.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.dalti.laposte.R.string;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.entity.AdminAPI;
import com.dalti.laposte.admin.model.SelectedActivation;
import com.dalti.laposte.core.entity.Activation;
import com.dalti.laposte.core.repositories.ActivationDAO;
import com.dalti.laposte.core.entity.ActivationsInfo;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.Pair;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.backend.DatabaseUtils;
import dz.jsoftware95.silverbox.android.backend.LiveListRepository;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public class ActivationsListRepository extends LiveListRepository<Activation, ActivationDAO> {

    private static final LiveData<SelectedActivation> EMPTY_SELECTION = new MutableLiveData<>(null);

    @NonNull
    private final Lazy<AdminAPI> adminAPI;
    @NonNull
    private final ExtraRepository extraRepository;
    @NonNull
    private final BuildConfiguration buildConf;
    @NonNull
    private final RepositoryUtil repositoryUtil;

    @Inject
    @AnyThread
    public ActivationsListRepository(@NonNull final Lazy<ActivationDAO> activationDAO,
                                     @NonNull final Lazy<AdminAPI> adminAPI,
                                     @NonNull final ExtraRepository extraRepository,
                                     @NonNull final BuildConfiguration buildConf,
                                     @NonNull final RepositoryUtil repositoryUtil) {
        super(activationDAO);
        this.adminAPI = adminAPI;
        this.extraRepository = extraRepository;
        this.buildConf = buildConf;
        this.repositoryUtil = repositoryUtil;
//        initializeIfNeeded();
    }

    @WorkerThread
    protected void onAutoRefresh() {
        ActivationDAO dao = requireDAO();
        if (dao.count() == 0)
            refresh(true);
        else {
            Long lastUpdate = dao.getLastActivationsUpdate();
            long maxCache = AppConfig.getInstance().getRemoteLong(LongSetting.MAX_ACTIVATION_CACHE);
            if (lastUpdate == null || (System.currentTimeMillis() - lastUpdate) > maxCache)
                refresh(true);
        }
    }

    @Override
    public void invalidate() {
        fetchActivations(true);
    }

    public void updateActivation(Map<String, String> data) {
        execute(newUpdateActivationJob(this, data));
    }

    private static Job newUpdateActivationJob(ActivationsListRepository repository,
                                              Map<String, String> data) {
        return new DuoDatabaseJob<ActivationsListRepository, Map<String, String>>(repository, data) {
            @Override
            protected void doFromBackground(@NonNull ActivationsListRepository repository, @NotNull Map<String, String> data) {
                Activation activation = Activation.parse(data);
                if (activation != null)
                    try {
                        repository.requireDAO().update(activation);
                    } finally {
                        repository.postPublish(DataEvent.DATA_FETCHED);
                    }
            }
        };
    }

    public void refresh(boolean feedback) {
        postPublish(DataEvent.FETCHING_DATA);
        fetchActivations(feedback);
    }

    public void fetchActivations(boolean feedback) {
        execute(new FetchActivationsJob(this, buildConf.getServerTarget(), feedback));
    }

    public LiveData<SelectedActivation> getSelectedActivation(Pair<Long, Integer> selectionInfo) {
        if (selectionInfo != null && selectionInfo.hasPrimaryValue()) {
            long activationID = selectionInfo.getPrimaryValue();
            Integer imageSize = selectionInfo.getSecondaryValue();
            MediatorLiveData<SelectedActivation> data = new MediatorLiveData<>();
            execute(new DuoDatabaseJob<ActivationsListRepository, MediatorLiveData<SelectedActivation>>(this, data) {
                @Override
                protected void doFromBackground(@NonNull ActivationsListRepository repository,
                                                @NonNull MediatorLiveData<SelectedActivation> data) {
                    ActivationDAO dao = repository.requireDAO();
                    new DuoJob<LiveData<Activation>, MediatorLiveData<SelectedActivation>>(dao.load(activationID), data) {
                        @Override
                        protected void doFromMain(@NotNull LiveData<Activation> activationData,
                                                  @NotNull MediatorLiveData<SelectedActivation> selectedActivationData) {
                            selectedActivationData.addSource(activationData,
                                    activation -> selectedActivationData.setValue(new SelectedActivation(activation, imageSize)));
                        }
                    }.execute();
                }
            });
            return data;
        } else
            return EMPTY_SELECTION;
    }

    private static final class FetchActivationsJob extends UnJob<ActivationsListRepository> {

        private static final String NAME = "fetch_activations_job";

        private final int serverTarget;
        private final boolean feedback;

        public FetchActivationsJob(ActivationsListRepository repository, int serverTarget, boolean feedback) {
            super(AppWorker.NETWORK, repository);
            this.serverTarget = serverTarget;
            this.feedback = feedback;
        }

        @Override
        protected void doFromBackground(@NonNull ActivationsListRepository repository) throws InterruptedException {
            Call<ActivationsInfo> activationsCall = null;
            try {
                String username = repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME);
                String password = repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_PASSWORD);
                if (username == null || password == null) {
                    QueueUtils.toast(R.string.credentials_needed, feedback);
                    return;
                }

                activationsCall = repository.adminAPI.get().getActivations(username, password, serverTarget);
                String url = QueueUtils.getUrl(activationsCall);
                Response<ActivationsInfo> response = activationsCall.execute();

                if (response.isSuccessful()) {
                    ActivationsInfo responseBody = response.body();
                    if (responseBody != null) {
                        List<Activation> activations = responseBody.getActivations();
                        List<Activation> newActivationsList = DatabaseUtils.filterValid(activations);
                        try {
                            repository.waitForDAO(getClass().getSimpleName()).replaceAll(newActivationsList);
                            Teller.info("activations fetched successfully, count: " + newActivationsList.size());
                            QueueUtils.toast(string.activations_updated, feedback);
                        } catch (Exception e) {
                            Teller.warn("failed to save fetched activations to the database", e);
                            QueueUtils.toast(string.could_not_persist_data, feedback);
                        }
                    } else
                        Teller.logMissingInfo("null body", feedback);

                    repository.repositoryUtil.handleResponse(responseBody, NAME);
                } else
                    repository.repositoryUtil.handleUnsuccessfulResponse(response, "could not update activation list: " + QueueUtils.getUrl(activationsCall), null, feedback, NAME);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "error while updating activation list: " + QueueUtils.getUrl(activationsCall), feedback);
            } finally {
                repository.postPublish(DataEvent.DATA_FETCHED);
            }
        }
    }
}
