package com.dalti.laposte.admin.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.dalti.laposte.R;
import com.dalti.laposte.admin.entity.AdminAPI;
import com.dalti.laposte.admin.model.LiveUser;
import com.dalti.laposte.core.repositories.AbstractUpdateHandler;
import com.dalti.laposte.core.repositories.AdminAction;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.InputProperty;
import com.dalti.laposte.core.repositories.IntegerSetting;
import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.repositories.ProgressDAO;
import com.dalti.laposte.core.entity.Service;
import com.dalti.laposte.core.repositories.StateDAO;
import com.dalti.laposte.core.repositories.StateRepository;
import com.dalti.laposte.core.repositories.StringNumberSetting;
import com.dalti.laposte.core.repositories.StringSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.BasicHandler;
import com.dalti.laposte.core.ui.NoteState;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.IdentityManager;
import dz.jsoftware95.queue.api.Situation;
import dz.jsoftware95.queue.common.Supplier;
import dz.jsoftware95.queue.api.UpdateResult;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.observers.LiveDataUpdater;
import retrofit2.Call;
import retrofit2.Response;

public class AdminDashboardRepository extends LazyRepository<ProgressDAO> {

    @NonNull
    private final ExtraRepository extraRepository;
    @NonNull
    private final AbstractUpdateHandler updateHandler;
    @NonNull
    private final Lazy<AdminAPI> adminAPI;
    @NonNull
    private final BuildConfiguration buildConf;
    private final RepositoryUtil repositoryUtil;

    private final MediatorLiveData<String> user;

    private final StateRepository stateRepository;

    @Inject
    @AnyThread
    public AdminDashboardRepository(@NonNull ExtraRepository extraRepository,
                                    @NonNull AbstractUpdateHandler updateHandler,
                                    @NonNull Lazy<ProgressDAO> progressDAO,
                                    @NonNull Lazy<AdminAPI> adminAPI,
                                    @NonNull BuildConfiguration buildConf,
                                    @NonNull RepositoryUtil repositoryUtil,
                                    @NonNull StateRepository stateRepository) {
        super(progressDAO);
        this.extraRepository = extraRepository;
        this.updateHandler = updateHandler;
        this.adminAPI = adminAPI;
        this.buildConf = buildConf;
        this.repositoryUtil = repositoryUtil;
        this.stateRepository = stateRepository;
        this.user = new MediatorLiveData<>();
        newInitJob(this).execute();
    }

    private static Job newInitJob(final AdminDashboardRepository repository) {
        return new UnJob<AdminDashboardRepository>(repository) {
            @Override
            protected void doFromMain(@NotNull AdminDashboardRepository repository) {
                LiveUser source = new LiveUser(repository.extraRepository.getString(StringSetting.CONTACT_PHONE),
                        repository.extraRepository.get(InputProperty.PRINCIPAL_NAME));
                repository.user.addSource(source, new LiveDataUpdater<>(repository.user));
            }
        };
    }

    public LiveData<String> getUser() {
        return user;
    }

    @NonNull
    private Lazy<AdminAPI> getAdminAPI() {
        return adminAPI;
    }

    @WorkerThread
    private Situation newSituation(StateDAO stateDAO) {
        return QueueUtils.newSituation(stateDAO, buildConf);
    }

    public void setToken(AdminAction action, long progressID) {
        execute(new SetTokenJob(this, action, progressID, buildConf.getServerTarget()));
    }

    public void setNote(long serviceID, Integer noteState, Long closeTime,
                        String noteEng, String noteFre, String noteArb) {
        execute(new SetNoteJob(this, serviceID, buildConf.getServerTarget(), noteEng, noteFre, noteArb, noteState, closeTime));
    }

    public void resetToken(long progressID) {
        execute(new ResetTokenJob(this, progressID, buildConf.getServerTarget()));
    }

    @WorkerThread
    private void executeUpdate(Call<UpdateResult> updateCall, Supplier<Job> retryJobSupplier, String operation) throws InterruptedException {
        try {
            Response<UpdateResult> response = updateCall.execute();
            if (response.isSuccessful()) {
                UpdateResult updateResult = response.body();
                if (updateResult != null) {
                    Map<String, String> data = updateResult.getData();
                    if (data != null) {
                        Teller.info("progress update sent: " + updateResult.toString());
                        AppConfig appConfig = AppConfig.getInstance();
                        if (appConfig.get(BooleanSetting.IMMEDIATE_FEEDBACK))
                            updateHandler.handleUpdate(data);
                        if (appConfig.get(BooleanSetting.VIBRATE_ON_UPDATE_SENT))
                            QueueUtils.vibrate(appConfig.getInt(IntegerSetting.UPDATE_SENT_VIBRATION_DURATION));
                    } else
                        Teller.logMissingInfo("null data");
                } else
                    Teller.logMissingInfo("null response body");

                repositoryUtil.handleResponse(updateResult, operation);
            } else
                repositoryUtil.handleUnsuccessfulResponse(response, retryJobSupplier, operation);
        } catch (Exception e) {
            repositoryUtil.handleRequestException(e, "could not send admin update: " + QueueUtils.getUrl(updateCall), true);
        }
    }

    private static class ResetTokenJob extends UnJob<AdminDashboardRepository> {

        private static final String NAME = "reset_token_job";
        private final long progressID;
        private final int serverTarget;
        private final boolean original;

        public ResetTokenJob(AdminDashboardRepository repository, long progressID, int serverTarget) {
            super(AppWorker.NETWORK, repository);
            this.progressID = progressID;
            this.serverTarget = serverTarget;
            this.original = true;
            BasicHandler.showNetworkIndicator();
        }

        public ResetTokenJob(AdminDashboardRepository repository, ResetTokenJob originalJob) {
            super(AppWorker.NETWORK, repository);
            this.progressID = originalJob.progressID;
            this.serverTarget = originalJob.serverTarget;
            this.original = false;
        }

        @Override
        protected void doFromBackground(@NonNull AdminDashboardRepository repository) throws InterruptedException {
            Call<UpdateResult> updateCall = null;
            try {
                BasicHandler.showNetworkIndicator();
                ProgressDAO dao = repository.waitForDAO(getClass().getSimpleName());
                Service service = dao.getServiceValue(IdentityManager.getServiceID(progressID));
                if (service != null) {
                    StateDAO stateDAO = repository.stateRepository.waitForDAO(getClass().getSimpleName());
                    Situation situation = repository.newSituation(stateDAO);
                    String username = repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME);
                    String password = repository.extraRepository.getAndWait(InputProperty.PRINCIPAL_PASSWORD);
                    if (username != null && password != null) {
                        updateCall = repository.getAdminAPI().get().resetTokens(progressID, username, password, serverTarget, situation);
                        repository.executeUpdate(updateCall,
                                () -> original ? new ResetTokenJob(repository, this) : null, NAME);
                    } else
                        QueueUtils.toast(com.dalti.laposte.admin.R.string.credentials_needed);
                } else
                    QueueUtils.toast(R.string.service_not_found);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "error while resetting office: " + QueueUtils.getUrl(updateCall), true);
            } finally {
                BasicHandler.hideNetworkIndicator();
            }
        }
    }

    private static class SetNoteJob extends UnJob<AdminDashboardRepository> {

        private static final String NAME = "set_note_job";

        private final long serviceID;
        private final int serverTarget;
        private final boolean original;

        private final String noteEng;
        private final String noteFre;
        private final String noteArb;
        private final Integer noteState;
        private final Long closeTime;

        protected SetNoteJob(AdminDashboardRepository repository, long serviceID, int serverTarget,
                             String noteEng, String noteFre, String noteArb,
                             Integer noteState, Long closeTime) {
            super(AppWorker.NETWORK, repository);

            this.serviceID = serviceID;
            this.serverTarget = serverTarget;
            this.original = true;

            this.noteEng = noteEng;
            this.noteFre = noteFre;
            this.noteArb = noteArb;
            this.noteState = noteState;
            this.closeTime = closeTime;
            BasicHandler.showNetworkIndicator();
        }

        protected SetNoteJob(AdminDashboardRepository repository, SetNoteJob originalJob) {
            super(AppWorker.NETWORK, repository);

            this.serviceID = originalJob.serviceID;
            this.serverTarget = originalJob.serverTarget;
            this.original = false;

            this.noteEng = originalJob.noteEng;
            this.noteFre = originalJob.noteFre;
            this.noteArb = originalJob.noteArb;
            this.noteState = originalJob.noteState;
            this.closeTime = originalJob.closeTime;
        }

        @Override
        protected void doFromBackground(@NonNull AdminDashboardRepository repository) throws InterruptedException {
            Call<UpdateResult> updateCall = null;
            try {
                BasicHandler.showNetworkIndicator();
                AppConfig appConfig = AppConfig.getInstance();
                appConfig.commit(StringSetting.NOTE_ENG, noteEng);
                appConfig.commit(StringSetting.NOTE_FRE, noteFre);
                appConfig.commit(StringSetting.NOTE_ARB, noteArb);
                appConfig.commit(StringNumberSetting.NOTE_STATE, StringUtil.toString(noteState, NoteState.DEFAULT_ICON_STATE_STRING));

                ProgressDAO dao = repository.waitForDAO(getClass().getSimpleName());
                Service service = dao.getServiceValue(serviceID);
                if (service != null) {
                    StateDAO stateDAO = repository.stateRepository.waitForDAO(getClass().getSimpleName());
                    Situation situation = repository.newSituation(stateDAO);

                    ExtraRepository extraRepository = repository.extraRepository;
                    String username = extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME);
                    String password = extraRepository.getAndWait(InputProperty.PRINCIPAL_PASSWORD);

                    if (username != null && password != null) {
                        updateCall = repository.getAdminAPI().get().setNote(
                                service.getId(),
                                GlobalUtil.trim(noteEng, GlobalConf.NULL_NOTE),
                                GlobalUtil.trim(noteFre, GlobalConf.NULL_NOTE),
                                GlobalUtil.trim(noteArb, GlobalConf.NULL_NOTE),
                                noteState, closeTime,
                                username, password,
                                serverTarget, situation);

                        repository.executeUpdate(updateCall,
                                () -> original ? new SetNoteJob(repository, this) : null, NAME);
                    } else
                        QueueUtils.toast(com.dalti.laposte.admin.R.string.credentials_needed);
                } else
                    QueueUtils.toast(R.string.service_not_found);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "error while setting office note: " + QueueUtils.getUrl(updateCall), true);
            } finally {
                BasicHandler.hideNetworkIndicator();
            }
        }
    }

    private static class SetTokenJob extends DuoJob<AdminDashboardRepository, AdminAction> {
        private static final String NAME = "set_token_job";
        private final long progressID;
        private final int serverTarget;
        private final boolean original;

        public SetTokenJob(AdminDashboardRepository repository, AdminAction action, long progressID, int serverTarget) {
            super(AppWorker.NETWORK, repository, action);
            this.progressID = progressID;
            this.serverTarget = serverTarget;
            this.original = true;
            BasicHandler.showNetworkIndicator();
        }

        public SetTokenJob(AdminDashboardRepository repository, AdminAction action, SetTokenJob originalJob) {
            super(AppWorker.NETWORK, repository, action);
            this.progressID = originalJob.progressID;
            this.serverTarget = originalJob.serverTarget;
            this.original = false;
        }

        @Override
        protected void doFromBackground(@NonNull AdminDashboardRepository repository, @NotNull AdminAction action) throws InterruptedException {
            Call<UpdateResult> updateCall = null;
            try {
                BasicHandler.showNetworkIndicator();
                ProgressDAO dao = repository.waitForDAO(getClass().getSimpleName());
                Service service = dao.getServiceValue(IdentityManager.getServiceID(progressID));
                if (service != null) {
                    StateDAO stateDAO = repository.stateRepository.waitForDAO(getClass().getSimpleName());
                    Situation situation = repository.newSituation(stateDAO);

                    ExtraRepository extraRepository = repository.extraRepository;
                    String username = extraRepository.getAndWait(InputProperty.PRINCIPAL_NAME);
                    String password = extraRepository.getAndWait(InputProperty.PRINCIPAL_PASSWORD);
                    if (username != null && password != null) {
                        Progress progress = dao.requireProgressValue(progressID);

                        progress.update(action);
                        service.update(action);
                        updateCall = repository.getAdminAPI().get().setTokens(
                                progress.getId(),
                                progress.getCurrentToken(),
                                progress.getWaiting(),
                                service.getAvailability(),
                                username,
                                password,
                                serverTarget, situation);

                        repository.executeUpdate(updateCall, () -> original ? new SetTokenJob(repository, action, this) : null, NAME);
                    } else
                        QueueUtils.toast(com.dalti.laposte.admin.R.string.credentials_needed);
                } else
                    QueueUtils.toast(R.string.service_not_found);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "error while setting office state: " + QueueUtils.getUrl(updateCall), true);
            } finally {
                BasicHandler.hideNetworkIndicator();
            }
        }
    }
}
