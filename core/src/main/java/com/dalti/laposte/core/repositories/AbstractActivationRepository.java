package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.dalti.laposte.R;
import com.dalti.laposte.core.ui.BasicHandler;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.dalti.laposte.core.util.RepositoryUtil;

import java.util.Map;

import javax.inject.Singleton;

import dz.jsoftware95.queue.common.Executable;
import dz.jsoftware95.queue.api.Pair;
import dz.jsoftware95.queue.common.CodeActivationResult;
import dz.jsoftware95.queue.common.ResponseConfig;
import dz.jsoftware95.queue.api.ServerResponse;
import dz.jsoftware95.silverbox.android.backend.AbstractRepository;
import dz.jsoftware95.silverbox.android.backend.DataEvent;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public abstract class AbstractActivationRepository extends AbstractRepository {

    protected final BuildConfiguration buildConf;
    protected final RepositoryUtil repositoryUtil;
    protected final LiveStringProperty activeUsername = new LiveStringProperty(StringSetting.ACTIVE_USERNAME);

    @AnyThread
    public AbstractActivationRepository(@NonNull final BuildConfiguration buildConf,
                                        @NonNull final RepositoryUtil repositoryUtil) {
        this.buildConf = buildConf;
        this.repositoryUtil = repositoryUtil;
    }

    public LiveStringProperty getActiveUsername() {
        return activeUsername;
    }

    public abstract void handleActivationNeeded(ActivationState activationState, boolean feedback, String operation);

    public abstract void toastActivationNeeded(String operation);

    @NonNull
    protected abstract Pair<Call<ServerResponse>, Executable> callActivationAPI(String applicationID,
                                                                                int applicationVersion,
                                                                                int androidVersion,
                                                                                Long googleServicesVersion,
                                                                                int targetServer,
                                                                                boolean feedbackEnabled) throws InterruptedException;

    @MainThread
    public void activateApplication(boolean feedback, String source) {
        new SendActivationCodeJob(this, buildConf.getSignedVersionCode(), buildConf.getServerTarget(), feedback, source).execute();
    }

    public void activateApplicationNow(String source) {
        new SendActivationCodeJob(this, buildConf.getSignedVersionCode(), buildConf.getServerTarget(), false, source).run();
    }

    public void refreshCacheableData(boolean feedback) {
        // handle this only in admin impl
    }

    public static final class SendActivationCodeJob extends UnJob<AbstractActivationRepository> {
        private static final String NAME = "send_activation_code_job";
        private final int versionCode;
        private final int targetServer;
        private final boolean feedbackEnabled;
        private final String source;

        public SendActivationCodeJob(AbstractActivationRepository repository, int versionCode, int targetServer, boolean feedbackEnabled, String source) {
            super(AppWorker.NETWORK, repository);
            this.versionCode = versionCode;
            this.targetServer = targetServer;
            this.feedbackEnabled = feedbackEnabled;
            this.source = source;
            BasicHandler.showNetworkIndicator();
        }

        @Override
        protected void doFromBackground(@NonNull AbstractActivationRepository repository) throws InterruptedException {
            try {
                BasicHandler.showNetworkIndicator();
                Pair<Call<ServerResponse>, Executable> activationArgs = repository.callActivationAPI(
                        AppConfig.getInstance().getApplicationID(),
                        versionCode,
                        QueueUtils.getAndroidSdkVersion(),
                        QueueUtils.getGoogleServicesVersion(),
                        targetServer,
                        feedbackEnabled);

                Call<ServerResponse> call = activationArgs.getPrimaryValue();
                if (call == null)
                    return;

                String url = QueueUtils.getUrl(call);
                Response<ServerResponse> response = call.execute();
                if (response.isSuccessful()) {
                    ServerResponse responseBody = response.body();
                    if (responseBody != null) {
                        Map<String, String> data = responseBody.getData();
                        if (data != null) {
                            String result = data.get(ResponseConfig.ACTIVATION_RESULT);
                            if (StringUtil.notBlank(result))
                                handleActivationResult(repository, data, result, url, activationArgs.getSecondaryValue(), feedbackEnabled);
                            else
                                Teller.logMissingInfo("blank activation result: '" + result + "' for url: " + url, feedbackEnabled);
                        } else
                            Teller.logMissingInfo("null data, url: " + url, feedbackEnabled);
                    } else
                        Teller.logMissingInfo("null response body, url: " + url, feedbackEnabled);
                } else
                    repository.repositoryUtil.handleUnsuccessfulResponse(response, "activation failed, url: " + url, null, feedbackEnabled, NAME);
            } catch (Exception e) {
                repository.repositoryUtil.handleRequestException(e, "could not activate the app", feedbackEnabled);
            } finally {
                repository.postPublish(DataEvent.DATA_FETCHED);
                BasicHandler.hideNetworkIndicator();
            }
        }

        @WorkerThread
        private void handleActivationResult(@NonNull AbstractActivationRepository repository,
                                            Map<String, String> data,
                                            String result,
                                            String url,
                                            Executable activationListener,
                                            boolean feedbackEnabled) throws LocalPersistentException, InterruptedException {

            CodeActivationResult activationOutcome = CodeActivationResult.fromString(result);

            if (activationOutcome == CodeActivationResult.CODE_ACTIVATED_SUCCESSFULLY) {
                AppConfig appConfig = AppConfig.getInstance();
                Long key = StringUtil.parseLong(data.get(ResponseConfig.ACTIVATION_KEY));
                Long expirationDate = StringUtil.parseLong(data.get(ResponseConfig.EXPIRATION_DATE));
                ActivationState activationState = appConfig.updateActivationState(key, expirationDate);
                if (activationState.isActive()) {
                    QueueUtils.toast(R.string.application_activated, feedbackEnabled);
                    repository.postPublish(DataEvent.SUCCESSFUL_UPDATE);
                    if (activationListener != null)
                        activationListener.exe();

                    Teller.logActivation(data, source);
                } else if (activationState.isExpired()) {
                    QueueUtils.toastActivationOutdated(activationState.getExpirationDate(), feedbackEnabled);
                    Teller.logUnexpectedCondition("activation code submitted recently is not active: " + activationState + ", url: " + url);
                } else
                    Teller.logMissingInfo("invalid activation info key: " + key + " exp-date: " + TimeUtils.formatAsDateTime(expirationDate) + " for url: " + url, feedbackEnabled);
            } else {
                Long activationDate = StringUtil.parseLong(data.get(ResponseConfig.ACTIVATION_DATE));

                if (activationOutcome == CodeActivationResult.CODE_ALREADY_ACTIVATED) {
                    if (feedbackEnabled)
                        if (activationDate == null)
                            QueueUtils.toast(R.string.activation_code_already_used);
                        else
                            QueueUtils.toast(R.string.activation_code_already_used_since, TimeUtils.formatAsDateTime(activationDate));
                } else if (activationOutcome == CodeActivationResult.INVALID_CODE) {
                    QueueUtils.toast(R.string.invalid_activation_code, feedbackEnabled);
                } else if (activationOutcome == CodeActivationResult.MISSING_APP_ID) {
                    QueueUtils.toast(R.string.google_services_no_connection, feedbackEnabled);
                } else if (activationOutcome == CodeActivationResult.CODE_EXPIRED) {
                    QueueUtils.toast(R.string.expired_activation_code);
                } else {
                    QueueUtils.toast(R.string.wrong_activation_code, feedbackEnabled);
                }

                Teller.logActivationRejected(String.valueOf(activationOutcome), activationDate);
            }
        }
    }
}

