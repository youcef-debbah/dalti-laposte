package com.dalti.laposte.core.util;

import androidx.annotation.WorkerThread;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.AbstractActivationRepository;
import com.dalti.laposte.core.repositories.ActivationState;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.BooleanSetting;
import com.dalti.laposte.core.repositories.ExtraRepository;
import com.dalti.laposte.core.repositories.LocalPersistentException;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.Service;
import com.dalti.laposte.core.repositories.ServicesListRepository;
import com.dalti.laposte.core.repositories.SimpleProperty;
import com.dalti.laposte.core.repositories.StateRepository;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.common.BadStringFormatException;
import dz.jsoftware95.queue.common.Supplier;
import dz.jsoftware95.queue.response.ResponseCode;
import dz.jsoftware95.queue.response.ResponseConfig;
import dz.jsoftware95.queue.response.ServerResponse;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.middleware.BasicActivity;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import okhttp3.ResponseBody;
import retrofit2.Response;

@Singleton
@WorkerThread
public class RepositoryUtil {

    private static final String SOURCE = "retry_handler";

    private final String LAST_ACTIVATION_EXPIRATION_RECHECK = RepositoryUtil.class.getName() + "#LAST_ACTIVATION_EXPIRATION_RECHECK";

    private final Lazy<ServicesListRepository> serviceRepository;

    private final ExtraRepository extraRepository;
    private final Lazy<AbstractActivationRepository> activationRepository;
    private final StateRepository stateRepository;
    private final BuildConfiguration buildConf;

    @Inject
    public RepositoryUtil(Lazy<ServicesListRepository> serviceRepository,
                          ExtraRepository extraRepository,
                          Lazy<AbstractActivationRepository> activationRepository,
                          StateRepository stateRepository,
                          BuildConfiguration buildConf) {
        this.serviceRepository = serviceRepository;
        this.extraRepository = extraRepository;
        this.activationRepository = activationRepository;
        this.stateRepository = stateRepository;
        this.buildConf = buildConf;
    }

    public <T extends ServerResponse> void handleResponse(T response, String operation) throws LocalPersistentException, InterruptedException {
        tryToHandleResponseCode(null, false, response, operation);
    }

    @WorkerThread
    public void handleUnsuccessfulResponse(Response<? extends ServerResponse> response, Supplier<Job> retryJobSupplier, String operation) throws LocalPersistentException, InterruptedException {
        handleUnsuccessfulResponse(response, "", retryJobSupplier, true, operation);
    }

    @WorkerThread
    public void handleUnsuccessfulResponse(Response<? extends ServerResponse> response,
                                           String message,
                                           Supplier<Job> retryJobSupplier,
                                           boolean feedbackEnabled,
                                           String operation) throws InterruptedException, LocalPersistentException {
        if (response.code() == ResponseConfig.SERVER_DOWN_CODE) {
            QueueUtils.toast(R.string.server_is_down, feedbackEnabled);
            Teller.info("server is down (" + response.code() + " " + response.message() + ") " + message);
        } else {
            if (tryToHandleResponseCode(retryJobSupplier, feedbackEnabled, getBody(response), operation))
                return;

            QueueUtils.toast(R.string.server_error, response.code(), feedbackEnabled);

            FirebaseCrashlytics crashlytics = Teller.getCrashlytics();
            if (crashlytics != null)
                crashlytics.log("raw error body: " + getRawErrorBody(response));

            String msg = "server error " + response.code() + " - " + response.message() + ", message: " + message;
            try {
                throw new RuntimeException(msg);
            } catch (RuntimeException e) {
                Teller.warn(msg, e);
            }
        }
    }

    public boolean tryToHandleResponseCode(Supplier<Job> retryJobSupplier, boolean feedbackEnabled, ServerResponse serverResponse, String operation) throws InterruptedException, LocalPersistentException {
        Integer code;
        if (serverResponse != null && (code = serverResponse.getCode()) != null) {
            if (code == ResponseCode.OK.code()) {
                ActivationState activationState = AppConfig.getInstance().getActivationState();
                if (!activationState.isActive())
                    handleActivationNeeded(Job::emptyJob, false, operation);
                return true;
            } else if (code == ResponseCode.ACTIVATION_NEEDED.code()) {
                Teller.info("activation needed");
                handleActivationNeeded(retryJobSupplier, feedbackEnabled, operation);
                return true;
            } else if (code == ResponseCode.ACTIVATION_OUTDATED.code()) {
                Teller.info("activation outdated");
                Long expirationDate = StringUtil.parseLong(StringUtil.getString(serverResponse.getData(), ResponseConfig.EXPIRATION_DATE));
                AppConfig.getInstance().updateActivationExpirationDate(expirationDate);
                if (extraRepository.getLongStore().containsKey(LAST_ACTIVATION_EXPIRATION_RECHECK)) {
                    QueueUtils.toastActivationOutdated(expirationDate, feedbackEnabled);
                    extraRepository.removeAndWait(SimpleProperty.CURRENT_ACTIVATED_CODE);
                } else {
                    extraRepository.getLongStore().put(LAST_ACTIVATION_EXPIRATION_RECHECK, System.currentTimeMillis());
                    handleActivationNeeded(retryJobSupplier, feedbackEnabled, operation);
                }
                return true;
            } else if (code == ResponseCode.CLIENT_VERSION_REJECTED.code()) {
                Teller.info("client version rejected");
                if (feedbackEnabled) {
                    Long migrationDate = StringUtil.parseLong(StringUtil.getString(serverResponse.getData(), ResponseConfig.MIGRATION_DATE));
                    if (migrationDate != null)
                        QueueUtils.toast(R.string.version_expired_since, TimeUtils.formatAsDateTime(migrationDate));
                    else
                        QueueUtils.toast(R.string.version_expired);

                    AtomicInteger updateNeededEncounters = AbstractQueueApplication.getUpdateNeededEncounters();
                    int updateNeededCount = updateNeededEncounters.incrementAndGet();

                    final AppConfig appConfig = AppConfig.getInstance();
                    BasicActivity currentActivity = BasicActivity.CURRENT_STARTED_ACTIVITY;
                    if (currentActivity != null && updateNeededCount > appConfig.getRemoteLong(LongSetting.UPDATE_HINTS_COUNT)) {
                        updateNeededEncounters.set(appConfig.getRemoteInt(LongSetting.UPDATE_HINTS_RESET));
                        QueueUtils.startPlayStore(currentActivity);
                    }
                }
                return true;
            } else if (code == ResponseCode.SERVICE_NOT_FOUND.code()) {
                Teller.info("service not found");
                serviceRepository.get().setCurrentService(null);
                QueueUtils.toast(R.string.service_not_found, feedbackEnabled);
                String id = StringUtil.getString(serverResponse.getData(), ResponseConfig.ENTRY_ID);
                Teller.logSelectContentEvent("not_found_" + id, Service.TABLE_NAME);
                QueueUtils.requestCacheInvalidation();
                return true;
            } else if (buildConf.isAdmin()) {
                if (code == ResponseCode.USER_NOT_FOUND.code()) {
                    Teller.info("user not found");
                    QueueUtils.toast(R.string.wrong_username, feedbackEnabled);
                    ensureNotActivated();
                    return true;
                } else if (code == ResponseCode.WRONG_PASSWORD.code()) {
                    Teller.info("wrong password");
                    QueueUtils.toast(R.string.wrong_password, feedbackEnabled);
                    ensureNotActivated();
                    return true;
                } else if (code == ResponseCode.PERMISSION_DENIED.code()) {
                    Teller.info("permission denied");
                    QueueUtils.toast(R.string.permission_denied, feedbackEnabled);
                    stateRepository.invalidateCacheOnly();
                    return true;
                }
            }
        }
        return false;
    }

    private void ensureNotActivated() throws LocalPersistentException {
        AppConfig appConfig = AppConfig.getInstance();
        if (appConfig.getActivationState().getKey() != null) {
            appConfig.resetActivation();
            stateRepository.invalidateCacheThenFetch(false);
        }
    }

    public void handleActivationNeeded(Supplier<Job> retryJobSupplier,
                                       boolean feedbackEnabled,
                                       String operation) throws InterruptedException, LocalPersistentException {
        Teller.info("resting local activation state");
        extraRepository.removeAndWait(SimpleProperty.CURRENT_ACTIVATED_CODE);
        ActivationState activationState = AppConfig.getInstance().resetActivation();

        AbstractActivationRepository activationRepository = this.activationRepository.get();
        if (AppConfig.getInstance().getRemoteBoolean(BooleanSetting.RETRY_WITH_IMPLICIT_ACTIVATION)) {
            Job retryJob = Supplier.get(retryJobSupplier);
            if (retryJob != null) {
                activationRepository.activateApplicationNow(SOURCE);
                retryJob.run();
                return;
            }
        }

        activationRepository.handleActivationNeeded(activationState, feedbackEnabled, operation);
    }

    private static ServerResponse getBody(Response<? extends ServerResponse> response) {
        ServerResponse body = response.body();
        if (body != null)
            return body;
        else {
            String rawErrorBody = getRawErrorBody(response);
            if (rawErrorBody != null)
                try {
                    return AbstractQueueApplication.parse(rawErrorBody, ServerResponse.class);
                } catch (JsonProcessingException e) {
                    Teller.error("could not parse error body as a ServerResponse: " + rawErrorBody, e);
                    return null;
                }
        }
        return null;
    }

    private static String getRawErrorBody(Response<? extends ServerResponse> response) {
        ResponseBody errorBody = response.errorBody();
        if (errorBody != null)
            try {
                return errorBody.string();
            } catch (RuntimeException | IOException e) {
                Teller.error("could not read raw error body as a string", e);
                return null;
            }

        return null;
    }

    public void handleRequestException(Exception e, String message) throws InterruptedException {
        handleRequestException(e, message, true);
    }

    public void handleRequestException(Exception e, String message, boolean feedbackEnabled) throws InterruptedException {
        Teller.info("handleRequestException: feedback=" + feedbackEnabled + ", message=" + message);
        if (e instanceof InterruptedException) {
            Teller.logInterruption((InterruptedException) e);
            throw (InterruptedException) e;
        } else if (e instanceof BadStringFormatException) {
            Teller.warn("error while parsing server response. " + message, e);
            QueueUtils.toast(R.string.error_while_parsing_response, feedbackEnabled);
        } else if (e instanceof LocalPersistentException) {
            Teller.warn("failed to persist data locally. " + message, e);
            QueueUtils.toast(R.string.could_not_persist_data, feedbackEnabled);
        } else {
            Teller.warn("network request error. " + message, e);
            if (QueueUtils.isCausedBy(e, QueueUtils.DNS_EXCEPTIONS))
                QueueUtils.toast(R.string.could_not_receive_response, feedbackEnabled);
            else
                QueueUtils.toast(R.string.could_not_receive_response, feedbackEnabled);
        }
    }
}
