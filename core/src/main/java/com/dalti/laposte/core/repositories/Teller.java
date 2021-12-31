package com.dalti.laposte.core.repositories;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.work.Data;

import com.dalti.laposte.R;
import com.dalti.laposte.core.entity.CoreAPI;
import com.dalti.laposte.core.entity.LoggedEvent;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.queue.api.EventInfo;
import dz.jsoftware95.queue.api.EventsList;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.queue.common.ResponseConfig;
import dz.jsoftware95.queue.api.ServerResponse;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import retrofit2.Call;
import retrofit2.Response;

@Singleton
@AnyThread
public class Teller extends LazyRepository<LogDAO> {

    public static final String ACTIVATION_SOURCE = "ACTIVATION_SOURCE";
    public static final String KEY_REQUEST_ID = "KEY_REQUEST_ID";
    public static final String KEY_WORKER_NAME = "KEY_WORKER_NAME";
    public static final String KEY_ENQUEUE_TIME = "KEY_ENQUEUE_TIME";

    public static final String TAG_PREFIX = "teller_";
    public static final String EVENT_TAG = TAG_PREFIX + "event";
    public static final String LOG_TAG = TAG_PREFIX + "log";
    private static final String ACTIVATION_CODE = "activation_code";
    public static final String DZ_JSOFTWARE_95 = "dz.jsoftware95.";
    public static final String COM_DALTI_LAPOSTE = "com.dalti.laposte.";
    private static volatile Teller INSTANCE = null;

    private final Lazy<LogDAO> logDAO;
    private final Lazy<CoreAPI> coreAPI;

    private volatile FirebaseAnalytics analytics;
    private volatile FirebaseCrashlytics crashlytics;

    @Inject
    public Teller(Lazy<LogDAO> logDAO, Lazy<CoreAPI> coreAPI) {
        super(logDAO);
        this.logDAO = logDAO;
        this.coreAPI = coreAPI;
        INSTANCE = this;
        execute(new ContextInitLogger());
    }

    public static void logMissingInfo(String logMessage, boolean feedbackEnabled) {
        logUnexpectedCondition(logMessage);
        QueueUtils.toast(R.string.server_response_incomplete, feedbackEnabled);
    }

    public static void logMissingInfo(String logMessage) {
        logMissingInfo(logMessage, true);
    }

    public static void logInterruption(InterruptedException e) {
        warn("Interrupted", e);
    }

    public static void logAppConfig(String key, Object value) {
        info("app config change: " + key + " -> " + value);
    }

    public static void handleFailedAppConfig(String key, Object value) {
        throw new LocalPersistentException("could not set '" + key + "' to: " + value);
    }

    public static void logDelayedException(AppConfig appConfig) {
        FirebaseCrashlytics crashlytics = getCrashlytics();
        if (crashlytics != null && appConfig != null) {
            AppConfig.DelayedException delayedException = appConfig.getDelayedException();
            if (delayedException != null) {
                crashlytics.log(delayedException.toString());
                try {
                    throw new RuntimeException("delayed exception");
                } catch (RuntimeException e) {
                    crashlytics.recordException(e);
                }
            }
        }
    }

    @Override
    protected void ontInitialize() {
        final AbstractQueueApplication app = AbstractQueueApplication.getInstance();
        if (app != null)
            try {
                app.waitInit();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        try {
            crashlytics = FirebaseCrashlytics.getInstance();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        try {
            analytics = AbstractQueueApplication.getAnalytics();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        Log.i(LOG_TAG, "teller init (crashlytics: " + (crashlytics != null ? "on" : "off")
                + ", analytics: " + (analytics != null ? "on" : "off") + ")");
    }

    @WorkerThread
    public static void uploadLoggedEventsNow() {
        Teller teller = Teller.INSTANCE;
        if (teller != null) {
            AppConfig appConfig = AppConfig.findInstance();
            if (appConfig != null) {
                String applicationID = appConfig.getApplicationID();
                if (!GlobalUtil.isBlankToken(applicationID)) {
                    LogDAO dao = teller.logDAO.get();
                    List<LoggedEvent> loggedEvents = dao.getAll();
                    List<EventInfo> events = getEventsInfo(loggedEvents);
                    if (GlobalUtil.notEmpty(events)) {
                        EventsList eventsList = new EventsList(applicationID, events);
                        try {
                            Call<ServerResponse> call = teller.coreAPI.get().uploadEvents(eventsList);
                            Response<ServerResponse> response = call.execute();
                            if (response.isSuccessful())
                                dao.deleteAll(loggedEvents);
                        } catch (Exception e) {
                            warn("failed to upload: " + eventsList, e);
                        }
                    }
                }
            }
        }
    }

    private static List<EventInfo> getEventsInfo(List<LoggedEvent> loggedEvents) {
        ArrayList<EventInfo> eventsInfo = new ArrayList<>();
        if (loggedEvents != null)
            for (LoggedEvent loggedEvent : loggedEvents)
                if (loggedEvent != null)
                    eventsInfo.add(new EventInfo(loggedEvent.getId(), loggedEvent.getName(), loggedEvent.getParameters()));
        return eventsInfo;
    }

    private static Job newLogJob(Teller teller, Bundle params, String event, long eventID) {
        return new UnJob<Teller>(AppWorker.LOG, teller) {
            @Override
            protected void doFromBackground(@NotNull Teller teller) {
                LogDAO logDAO = teller.requireDAO();
                FirebaseAnalytics analytics = teller.analytics;

                String eventNameAndData = StringUtil.toString(event, params);
                String eventData = StringUtil.toDataString(params);
                Log.i(EVENT_TAG, eventNameAndData);
                logDAO.log(new LoggedEvent(eventID, event, eventData));
                if (analytics != null)
                    analytics.logEvent(event, params);
                else {
                    analytics = AbstractQueueApplication.getAnalytics();
                    teller.analytics = analytics;
                    handleLocalFailure(logDAO, analytics, "null analytics", analytics != null);
                }
            }

            @WorkerThread
            private void handleLocalFailure(LogDAO logDAO, FirebaseAnalytics analytics, String type, boolean handled) {
                Bundle params = new Bundle();
                params.putString(Event.LocalFailure.Param.TYPE, type);
                params.putBoolean(Event.LocalFailure.Param.AUTO_HANDLED, handled);
                String localFailureData = StringUtil.toDataString(params);

                logDAO.log(new LoggedEvent(GlobalUtil.randomLong(), Event.LocalFailure.NAME, localFailureData));
                warn(Event.LocalFailure.NAME + " with data: " + localFailureData);
                if (analytics != null)
                    analytics.logEvent(Event.LocalFailure.NAME, params);
            }
        };
    }

    public static void debug(String msg) {
        Log.d(LOG_TAG, msg);
        FirebaseCrashlytics crashlytics = getCrashlytics();
        if (crashlytics != null)
            crashlytics.log("DEBUG - " + msg);
    }

    public static void info(String msg) {
        Log.i(LOG_TAG, msg);
        FirebaseCrashlytics crashlytics = getCrashlytics();
        if (crashlytics != null)
            crashlytics.log("INFO - " + msg);
    }

    public static void warn(String msg) {
        Log.w(LOG_TAG, msg);
        FirebaseCrashlytics crashlytics = getCrashlytics();
        if (crashlytics != null)
            crashlytics.log("WARN - " + msg);
    }

    public static void warn(String msg, Throwable e) {
        if (e == null)
            warn(msg);
        else {
            FirebaseCrashlytics crashlytics = getCrashlytics();
            if (crashlytics != null)
                recordException(msg, e, crashlytics);
            else {
                Log.w(LOG_TAG, "WARN - (Null Crashlytics): " + msg, e);
                AppConfig appConfig = AppConfig.findInstance();
                if (appConfig != null)
                    appConfig.logDelayedException(msg + "; Delayed Exception: " + getSummary(e));
            }
        }
    }

    public static void recordException(@NonNull String msg, Throwable e, FirebaseCrashlytics crashlytics) {
        if (isRecordable(e)) {
            Log.w(LOG_TAG, "WARN - (Recorded): " + msg, e);
            crashlytics.log(msg);
            crashlytics.recordException(e);
        } else {
            Log.i(LOG_TAG, "WARN - (Unrecorded): " + msg, e);
            crashlytics.log(msg + "; Unrecorded Exception: " + getSummary(e));
        }
    }

    private static String getSummary(Throwable e) {
        StringBuilder summary = new StringBuilder(128);
        if (e != null) {
            summary.append(e.getClass().getName())
                    .append(": ")
                    .append(e.getMessage())
                    .append(getFirstRelatedStackStrace(e))
                    .append(' ')
            ;
            Throwable current = e.getCause();
            while (current != null) {
                summary.append("Caused by: ")
                        .append(current.getClass().getName())
                        .append(": ")
                        .append(current.getMessage())
                        .append(' ')
                ;
                current = current.getCause();
            }
        }
        return summary.toString();
    }

    private static String getFirstRelatedStackStrace(Throwable e) {
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement element : stackTrace)
            if (element != null) {
                final String className = element.getClassName();
                if (GlobalUtil.startWith(className, DZ_JSOFTWARE_95) || GlobalUtil.startWith(className, COM_DALTI_LAPOSTE))
                    return " at " + element.toString();
            }
        return "";
    }

    public static boolean isRecordable(Throwable e) {
        String ignoredExceptionsNames = getUnrecordedExceptionsNames();
        return StringUtil.isNullOrEmpty(ignoredExceptionsNames)
                || !QueueUtils.isCausedBy(e, ignoredExceptionsNames.split(GlobalConf.SEPARATOR));
    }

    public static String getUnrecordedExceptionsNames() {
        AppConfig appConfig = AppConfig.findInstance();
        if (appConfig != null)
            return appConfig.getRemoteString(StringSetting.UNRECORDED_EXCEPTIONS);
        else
            return StringSetting.UNRECORDED_EXCEPTIONS.getDefaultString();
    }

    public static void error(String msg, Throwable e) {
        if (e == null)
            warn(msg);
        else {
            Log.e(LOG_TAG, msg, e);
            FirebaseCrashlytics crashlytics = getCrashlytics();
            if (crashlytics != null)
                recordException("ERROR - " + msg, e, crashlytics);
        }
    }

    @Nullable
    public static FirebaseCrashlytics lookupCrashlytics() {
        try {
            return FirebaseCrashlytics.getInstance();
        } catch (RuntimeException e) {
            Log.w(LOG_TAG, "Crashlytics lookup failed", e);
            return null;
        }
    }

    @Nullable
    public static FirebaseCrashlytics getCrashlytics() {
        Teller teller = Teller.INSTANCE;
        if (teller != null) {
            FirebaseCrashlytics crashlytics = teller.crashlytics;
            if (crashlytics != null)
                return crashlytics;
        }

        return lookupCrashlytics();
    }

    public static void logEvent(String event, Bundle params) {
        logEvent(event, params, true);
    }

    public static void logEvent(String event, Bundle params, boolean immediate) {
        if (event != null) {
            Teller teller = INSTANCE;
            if (teller != null) {
                try {
                    Job logJob = newLogJob(teller, params, event, GlobalUtil.randomLong());
                    if (AppWorker.LOG.isCurrentThread())
                        logJob.run();
                    else if (immediate)
                        teller.execute(logJob);
                    else
                        teller.executeIfPossible(logJob);
                } catch (RuntimeException e) {
                    Bundle localFailureParams = new Bundle();
                    localFailureParams.putString(Event.LocalFailure.Param.TYPE, "execute(newLogJob) failure");
                    localFailureParams.putString(Event.LocalFailure.Param.PAYLOAD, StringUtil.toString(event, params));
                    String localFailureData = StringUtil.toDataString(params);
                    error(Event.LocalFailure.NAME + " with data: " + localFailureData, e);
                }
            } else
                warn("null teller - " + StringUtil.toString(event, params));
        } else
            logUnexpectedCondition();
    }

    public static void log(String event) {
        logEvent(event, null);
    }

    public static void log(String event, String paramKey, String paramValue) {
        Bundle params = new Bundle();
        params.putString(paramKey, paramValue);
        logEvent(event, params);
    }

    public static void log(String event, String paramKey, long paramValue) {
        Bundle params = new Bundle();
        params.putLong(paramKey, paramValue);
        logEvent(event, params);
    }

    public static void log(String event, String paramKey, double paramValue) {
        Bundle params = new Bundle();
        params.putDouble(paramKey, paramValue);
        logEvent(event, params);
    }

    public static void logActivation(Map<String, String> data, String source) {
        String code = StringUtil.getString(data, ResponseConfig.ACTIVATION_CODE);
        String transactionID = StringUtil.getString(data, ResponseConfig.TRANSACTION_ID);
        if (StringUtil.notBlank(code) && StringUtil.notBlank(transactionID)) {
            Bundle bundle = new Bundle();
            bundle.putString(Event.Purchase.Param.TRANSACTION_ID, transactionID);
            bundle.putString(Event.Purchase.Param.CURRENCY, StringUtil.getString(data, ResponseConfig.CURRENCY, "EUR"));
            bundle.putDouble(Event.Purchase.Param.VALUE, StringUtil.parseDouble(StringUtil.getString(data, ResponseConfig.PRICE), 0.625743));
            String admin = StringUtil.getString(data, ResponseConfig.ADMIN, "unknown");
            bundle.putString(Event.Purchase.Param.AFFILIATION, admin);
            bundle.putParcelableArray(Event.Purchase.Param.ITEMS, new Bundle[]{getActivationCodeAsItem(code, admin, data)});
            bundle.putString(Event.Purchase.Param.METHOD, source != null ? source : "other_activation");
            logEvent(Event.Purchase.NAME, bundle);
        } else if (StringUtil.isTrue(data, ResponseConfig.LOGGED_IN)) {
            Bundle bundle = new Bundle();
            bundle.putString(Event.Login.Param.METHOD, source != null ? source : "other_logging");
            logEvent(Event.Login.NAME, bundle);
        }
    }

    public static Bundle getActivationCodeAsItem(String code, String admin, Map<String, String> data) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.ItemParam.ITEM_NAME, "code_" + code);
        bundle.putString(Event.ItemParam.ITEM_CATEGORY, ACTIVATION_CODE);
        bundle.putInt(Event.ItemParam.QUANTITY, 1);
        bundle.putString(Event.ItemParam.AFFILIATION, admin);
        bundle.putString(Event.ItemParam.ITEM_VARIANT, getCodeVariant(data));
        bundle.putString(Event.ItemParam.COUPON, StringUtil.getString(data, ResponseConfig.COUPON, "none"));
        bundle.putInt(Event.ItemParam.DISCOUNT, StringUtil.parseInt(StringUtil.getString(data, ResponseConfig.DISCOUNT), 0));
        return bundle;
    }

    private static String getCodeVariant(Map<String, String> data) {
        Long activationDate = StringUtil.parseLong(data.get(ResponseConfig.ACTIVATION_DATE));
        Long expirationDate = StringUtil.parseLong(data.get(ResponseConfig.EXPIRATION_DATE));
        if (activationDate != null && expirationDate != null)
            return (TimeUnit.MILLISECONDS.toDays(expirationDate) - TimeUnit.MILLISECONDS.toDays(activationDate)) + "_days_subscription";
        else
            return activationDate + "->" + expirationDate;
    }

    public static void logScreenViewEvent(Context context, String name) {
        if (name != null && context instanceof Activity) {
            Bundle bundle = new Bundle();
            bundle.putString(Event.ScreenView.Param.SCREEN_CLASS, context.getClass().getSimpleName());
            bundle.putString(Event.ScreenView.Param.SCREEN_NAME, name);
            logEvent(Event.ScreenView.NAME, bundle);
        }
    }

    public static Data.Builder logWorkerRequest(@NotNull String workerName) {
        return logWorkerRequest(workerName, true);
    }

    public static Data.Builder logWorkerRequest(@NotNull String workerName, boolean immediate) {
        long now = System.currentTimeMillis();
        long id = GlobalUtil.randomLong(now);

        Bundle params = new Bundle();
        params.putString(Event.WorkerRequest.Param.WORKER_NAME, workerName);
        params.putLong(Event.WorkerRequest.Param.ID, id);
        params.putString(Event.WorkerRequest.Param.TIMESTAMP, TimeUtils.formatAsTimeExact(now));

        AppConfig appConfig = AppConfig.findInstance();
        if (appConfig != null)
            params.putString(Event.WorkerRequest.Param.UPTIME, TimeUtils.formatAsTimeExact(appConfig.uptime(now)));

        logEvent(Event.WorkerRequest.NAME, params, immediate);

        Data.Builder builder = new Data.Builder();
        return builder
                .putString(KEY_WORKER_NAME, workerName)
                .putLong(KEY_REQUEST_ID, id)
                .putLong(KEY_ENQUEUE_TIME, now)
                ;
    }

    public static void logWorkerSession(@NonNull Data data) {
        long now = System.currentTimeMillis();
        Bundle params = new Bundle();

        long enqueueTime = data.getLong(KEY_ENQUEUE_TIME, -1);
        if (enqueueTime > 0)
            params.putLong(Event.WorkerSession.Param.DELAY, now - enqueueTime);

        params.putLong(Event.WorkerSession.Param.ID, data.getLong(KEY_REQUEST_ID, -1));
        params.putString(Event.WorkerSession.Param.WORKER_NAME, String.valueOf(data.getString(KEY_WORKER_NAME)));
        logEvent(Event.WorkerSession.NAME, params);
    }

    public static void logUnexpectedNull(Object... objects) {
        StringBuilder builder = new StringBuilder();
        if (objects != null)
            for (Object object : objects)
                builder.append(object == null ? "null " : "not-null ");
        logUnexpectedCondition(builder.toString());
    }

    public static void logUnexpectedCondition() {
        try {
            throw new CreepyCornerException();
        } catch (CreepyCornerException e) {
            warn("Unexpected Condition", e);
        }
    }

    public static void logUnexpectedCondition(String condition) {
        String message = "Unexpected Condition: " + condition;
        try {
            throw new CreepyCornerException(message);
        } catch (CreepyCornerException e) {
            warn(message, e);
        }
    }

    public static void logSelectContentEvent(String id, String type) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.SelectContent.Param.ITEM_ID, id);
        bundle.putString(Event.SelectContent.Param.CONTENT_TYPE, type);
        logEvent(Event.SelectContent.NAME, bundle);
    }

    public static void logViewItemEvent(Bundle item) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArray(Event.ViewItem.Param.ITEMS, new Bundle[]{item});
        logEvent(Event.ViewItem.NAME, bundle);
    }

    public static Bundle newItem(Long id, String category, String category2) {
        Bundle item = new Bundle();
        item.putString(Event.ItemParam.ITEM_ID, String.valueOf(id));
        item.putString(Event.ItemParam.ITEM_CATEGORY, String.valueOf(category));
        if (category2 != null)
            item.putString(Event.ItemParam.ITEM_CATEGORY_2, category2);
        return item;
    }

    public static Bundle newItem(Long id, String category) {
        return newItem(id, category, null);
    }

    public static void logSetTicket(Long id, int ticket) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.SetTicket.Param.TICKET_ID, "ticket_" + id);
        bundle.putLong(Event.SetTicket.Param.TICKET_NUMBER, ticket);
        logEvent(Event.SetTicket.NAME, bundle);
    }

    public static void logClearTicket(Long id, String action) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.ClearTicket.Param.TICKET_ID, "ticket_" + id);
        bundle.putString(Event.ClearTicket.Param.TICKET_CLEARING_TRIGGER, String.valueOf(action));
        logEvent(Event.ClearTicket.NAME, bundle);
    }

    public static void logPermissionsDenied(Context context, String[] permissions) {
        if (permissions != null)
            for (String permission : permissions)
                if (!ContextUtils.isPermissionGranted(context, permission))
                    logPermissionDenied(permission);
    }

    public static void logPermissionDenied(String permission) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.DenyPermission.Param.DENIED_PERMISSION, String.valueOf(permission));
        logEvent(Event.DenyPermission.NAME, bundle);
    }

    public static void logSendSmsSucceed(Long id, String token) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.SmsSent.Param.SMS_ID, String.valueOf(id));
        bundle.putString(Event.SmsSent.Param.SMS_TOKEN, String.valueOf(token));
        logEvent(Event.SmsSent.NAME, bundle);
    }

    public static void logSendSmsFailure(Long id, String token, int code) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.SmsNotSent.Param.SMS_ID, String.valueOf(id));
        bundle.putString(Event.SmsNotSent.Param.SMS_TOKEN, String.valueOf(token));
        bundle.putString(Event.SmsNotSent.Param.SMS_OUTCOME, SmsRepository.getOutcome(code));
        logEvent(Event.SmsNotSent.NAME, bundle);
    }

    public static void logDeliveredSms(Long id, String token) {
        Bundle bundle = new Bundle();
        bundle.putString(Event.SmsDelivered.Param.SMS_ID, String.valueOf(id));
        bundle.putString(Event.SmsDelivered.Param.SMS_TOKEN, String.valueOf(token));
        logEvent(Event.SmsDelivered.NAME, bundle);
    }

    public static void logActivationRejected(String error, Long activationDate) {
        Bundle params = new Bundle();
        params.putString(Event.ActivationRejected.Param.ACTIVATION_OUTCOME, String.valueOf(error));
        if (activationDate != null && activationDate > 0)
            params.putString(Event.ActivationRejected.Param.ACTIVATION_DATE, TimeUtils.formatAsDateTime(activationDate));
        logEvent(Event.ActivationRejected.NAME, params);
    }

    public static void logClick(String name) {
        logClick(name, null);
    }

    public static void logClick(String name, String payload) {
        Bundle params = new Bundle();
        params.putString(Event.ControllerClick.Param.CONTROLLER_NAME, Objects.requireNonNull(name));
        if (payload != null)
            params.putString(Event.ControllerClick.Param.CONTROLLER_PAYLOAD, payload);
        logEvent(Event.ControllerClick.NAME, params);
    }
}
