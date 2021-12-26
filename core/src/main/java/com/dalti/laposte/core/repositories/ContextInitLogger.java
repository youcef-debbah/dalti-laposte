package com.dalti.laposte.core.repositories;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.webkit.WebViewCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.CustomKeysAndValues;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import dagger.Lazy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dz.jsoftware95.queue.common.Payload;
import dz.jsoftware95.queue.response.ServerResponse;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.BasicJob;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import retrofit2.Call;
import retrofit2.Response;

import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_DISABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED;
import static android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_WHITELISTED;

public class ContextInitLogger extends BasicJob {

    private static final String NO_APP = "no_app";
    private static final String NO_APP_CONFIG = "no_app_config";
    private static final String NO_BUILD_CONFIG = "no_build_config";
    private static final String NO_POWER_MANAGER = "no_power_manager";
    private static final String NO_CONNECTIVITY_MANAGER = "no_connectivity_manager";
    private static final String NOT_AVAILABLE = "N/A";
    public static final String ERROR = "error";

    private final String timestamp;

    public ContextInitLogger() {
        super(AppWorker.LOG);
        timestamp = TimeUtils.formatAsTimeExact(System.currentTimeMillis());
    }

    @Override
    protected void doFromBackground() {
        AppConfig appConfig = AppConfig.findInstance();
        if (appConfig != null)
            appConfig.setUserID(appConfig.getApplicationID());

        Bundle properties = getUserProperties(appConfig);
        setUserProperties(properties);

        addExtraInfo(properties, appConfig, timestamp);
        logCustomKeys(properties);

        Teller.logDelayedException(appConfig);
        Teller.logEvent(Event.ContextInit.NAME, properties);
    }

    public static Bundle getAllUserProperties() {
        AppConfig appConfig = AppConfig.findInstance();
        final Bundle properties = getUserProperties(appConfig);
        addExtraInfo(properties, appConfig, TimeUtils.formatAsTimeExact(System.currentTimeMillis()));
        properties.putString(UserProperty.APPLICATION_ID, appConfig != null ? appConfig.getApplicationID() : NO_APP_CONFIG);
        return properties;
    }

    private static void addExtraInfo(Bundle properties, AppConfig appConfig, String timestamp) {
        properties.putString(Event.ContextInit.Param.TIMESTAMP, timestamp);
        if (appConfig != null) {
            properties.putString(StringSetting.CONTACT_PHONE.name(), appConfig.get(StringSetting.CONTACT_PHONE));
        } else {
            properties.putString(StringSetting.CONTACT_PHONE.name(), NO_APP_CONFIG);
        }
    }

    private static Bundle getUserProperties(AppConfig appConfig) {
        Bundle properties = new Bundle();

        if (appConfig != null) {
            ActivationState activationState = appConfig.getActivationState();
            properties.putString(UserProperty.ACTIVATION_STATE, activationState.isActive() ? "active" : "not active");
            properties.putString(UserProperty.REMOTE_CONFIG_VERSION, appConfig.getRemoteString(StringSetting.REMOTE_CONFIG_VERSION));
            properties.putString(UserProperty.IN_APP_MESSAGE_ID, appConfig.getInAppMessageID());
            properties.putString(UserProperty.LAST_RECEIVED_ALARM, appConfig.get(StringSetting.LAST_RECEIVED_ALARM));
            properties.putString(UserProperty.USER_RATING, appConfig.get(StringSetting.USER_RATING));
        } else {
            properties.putString(UserProperty.ACTIVATION_STATE, NO_APP_CONFIG);
            properties.putString(UserProperty.REMOTE_CONFIG_VERSION, NO_APP_CONFIG);
            properties.putString(UserProperty.IN_APP_MESSAGE_ID, NO_APP_CONFIG);
            properties.putString(UserProperty.LAST_RECEIVED_ALARM, NO_APP_CONFIG);
            properties.putString(UserProperty.USER_RATING, NO_APP_CONFIG);
        }

        try {
            PackageInfo webViewPackageInfo = WebViewCompat.getCurrentWebViewPackage(AbstractQueueApplication.requireInstance());
            if (webViewPackageInfo != null)
                properties.putString(UserProperty.WEB_VIEW_VERSION, String.valueOf(webViewPackageInfo.versionName));
            else
                properties.putString(UserProperty.WEB_VIEW_VERSION, "no_web_package_info");
        } catch (RuntimeException e) {
            Teller.warn("web info collections failed", e);
            properties.putString(UserProperty.WEB_VIEW_VERSION, ERROR);
        }

        AbstractQueueApplication application = AbstractQueueApplication.getInstance();
        if (application != null) {
            properties.putString(UserProperty.ZEN_MODE, getZenModeLabel(application));
            properties.putString(UserProperty.GOOGLE_SERVICES_VERSION, String.valueOf(QueueUtils.getGoogleServicesVersion(application)));

            ConnectivityManager connectivityManager = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
            properties.putString(UserProperty.RESTRICT_BACKGROUND_MODE, getRestrictBackgroundMode(connectivityManager));
            properties.putString(UserProperty.ACTIVE_NETWORK_TYPE, getNetworkType(connectivityManager));

            PowerManager powerManager = (PowerManager) application.getSystemService(Context.POWER_SERVICE);
            properties.putString(UserProperty.POWER_SAFE_MODE_ENABLED, String.valueOf(powerManager.isPowerSaveMode()));
            properties.putString(UserProperty.IGNORE_BATTERY_OPTIMIZER, getIgnoreBatteryOptimizer(powerManager, application));
            properties.putString(UserProperty.PERFORMANCE_MODE_ENABLED, getPerformanceModeEnabled(powerManager));
            properties.putString(UserProperty.GOOGLE_SERVICES_AVAILABILITY, getGoogleServicesAvailability(application));

            properties.putString(UserProperty.AIRPLANE_MODE_STATE, ContextUtils.isPlainModeOn(application) ? "airplane_mode_on" : "airplane_mode_off");
            properties.putString(UserProperty.WIFI_STATE, ContextUtils.isWifiOn(application) ? "wifi_on" : "wifi_off");
            properties.putString(UserProperty.DATA_ROAMING_STATE, ContextUtils.isDataRoamingOn(application) ? "data_roaming_on" : "data_roaming_off");
            properties.putString(UserProperty.NIGHT_MODE, getNightMode(application));
            properties.putString(UserProperty.CONTEXT_INIT_DURATION, String.valueOf(application.getInitDuration()));

            final BuildConfiguration buildConfiguration = AbstractQueueApplication.getCurrentBuildConfiguration();
            if (buildConfiguration != null) {
                properties.putString(UserProperty.APPLICATION_VERSION, buildConfiguration.getFullVersionName());
            } else {
                properties.putString(UserProperty.APPLICATION_VERSION, NO_BUILD_CONFIG);
            }
        } else {
            properties.putString(UserProperty.ZEN_MODE, NO_APP);
            properties.putString(UserProperty.GOOGLE_SERVICES_VERSION, NO_APP);
            properties.putString(UserProperty.RESTRICT_BACKGROUND_MODE, NO_APP);
            properties.putString(UserProperty.ACTIVE_NETWORK_TYPE, NO_APP);
            properties.putString(UserProperty.POWER_SAFE_MODE_ENABLED, NO_APP);
            properties.putString(UserProperty.IGNORE_BATTERY_OPTIMIZER, NO_APP);
            properties.putString(UserProperty.PERFORMANCE_MODE_ENABLED, NO_APP);
            properties.putString(UserProperty.GOOGLE_SERVICES_AVAILABILITY, NO_APP);
            properties.putString(UserProperty.AIRPLANE_MODE_STATE, NO_APP);
            properties.putString(UserProperty.WIFI_STATE, NO_APP);
            properties.putString(UserProperty.DATA_ROAMING_STATE, NO_APP);
            properties.putString(UserProperty.NIGHT_MODE, NO_APP);
            properties.putString(UserProperty.CONTEXT_INIT_DURATION, NO_APP);
            properties.putString(UserProperty.APPLICATION_VERSION, NO_APP);
        }

        return properties;
    }

    private static String getGoogleServicesAvailability(Context context) {
        try {
            switch (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                case ConnectionResult.SUCCESS:
                    return "available";
                case ConnectionResult.SERVICE_MISSING:
                    return "missing";
                case ConnectionResult.SERVICE_UPDATING:
                    return "updating";
                case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                    return "update_required";
                case ConnectionResult.SERVICE_DISABLED:
                    return "disabled";
                case ConnectionResult.SERVICE_INVALID:
                    return "invalid";
                default:
                    return "unknown";
            }
        } catch (RuntimeException e) {
            Teller.warn("failed to get google services availability", e);
            return "error";
        }
    }

    private static String getPerformanceModeEnabled(PowerManager powerManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            if (powerManager != null)
                return String.valueOf(powerManager.isSustainedPerformanceModeSupported());
            else
                return NO_POWER_MANAGER;
        else
            return NOT_AVAILABLE;
    }

    private static String getIgnoreBatteryOptimizer(PowerManager powerManager, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (powerManager != null)
                return String.valueOf(powerManager.isIgnoringBatteryOptimizations(context.getPackageName()));
            else
                return NO_POWER_MANAGER;
        } else
            return NOT_AVAILABLE;
    }

    private static String getNetworkType(ConnectivityManager connectivityManager) {
        if (connectivityManager == null)
            return NO_CONNECTIVITY_MANAGER;
        else
            return connectivityManager.isActiveNetworkMetered() ? "metered" : "unmetered";
    }

    private static String getRestrictBackgroundMode(ConnectivityManager connectivityManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (connectivityManager != null)
                switch (connectivityManager.getRestrictBackgroundStatus()) {
                    case RESTRICT_BACKGROUND_STATUS_ENABLED:
                        // Background data usage is blocked for this app. Wherever possible,
                        // the app should also use less data in the foreground.
                        return "restrict_enabled";
                    case RESTRICT_BACKGROUND_STATUS_WHITELISTED:
                        // The app is allowed to bypass Data Saver. Nevertheless, wherever possible,
                        // the app should use less data in the foreground and background.
                        return "restrict_whitelisted";
                    case RESTRICT_BACKGROUND_STATUS_DISABLED:
                        // Data Saver is disabled. Since the device is connected to a
                        // metered network, the app should use less data wherever possible.
                        return "restrict_disabled";
                    default:
                        return "other";
                }
            else
                return NO_CONNECTIVITY_MANAGER;
        } else
            return NOT_AVAILABLE;
    }

    private void setUserProperties(Bundle properties) {
        FirebaseAnalytics analytics = AbstractQueueApplication.getAnalytics();
        if (analytics != null)
            for (String key : properties.keySet())
                analytics.setUserProperty(key, String.valueOf(properties.get(key)));
    }

    private void logCustomKeys(Bundle customKeys) {
        FirebaseCrashlytics crashlytics = Teller.getCrashlytics();
        if (crashlytics != null) {
            CustomKeysAndValues.Builder builder = new CustomKeysAndValues.Builder();
            for (String key : customKeys.keySet())
                builder.putString(key, String.valueOf(customKeys.get(key)));
            crashlytics.setCustomKeys(builder.build());
        }
    }

    interface UserProperty {
        // this is not included with the other properties
        String APPLICATION_ID = "APPLICATION_ID";

        String APPLICATION_VERSION = "APPLICATION_VERSION";
        String ACTIVATION_STATE = "ACTIVATION_STATE";

        String RESTRICT_BACKGROUND_MODE = "RESTRICT_BACKGROUND_MODE";
        String ACTIVE_NETWORK_TYPE = "ACTIVE_NETWORK_TYPE";

        String ZEN_MODE = "ZEN_MODE";

        String POWER_SAFE_MODE_ENABLED = "POWER_SAFE_MODE_ENABLED";
        String PERFORMANCE_MODE_ENABLED = "PERFORMANCE_MODE_ENABLED";

        String IGNORE_BATTERY_OPTIMIZER = "IGNORE_BATTERY_OPTIMIZER";

        String GOOGLE_SERVICES_VERSION = "GOOGLE_SERVICES_VERSION";
        String REMOTE_CONFIG_VERSION = "REMOTE_CONFIG_VERSION";

        String GOOGLE_SERVICES_AVAILABILITY = "GOOGLE_SERVICES_STATE";
        String LAST_RECEIVED_ALARM = "LAST_ALARM_TYPE";

        String WIFI_STATE = "WIFI_STATE";
        String DATA_ROAMING_STATE = "DATA_ROAMING_STATE";
        String AIRPLANE_MODE_STATE = "AIRPLANE_MODE_STATE";

        String IN_APP_MESSAGE_ID = "IN_APP_MESSAGE_ID";

        String WEB_VIEW_VERSION = "WEB_VIEW_PACKAGE";

        String USER_RATING = "USER_RATING";
        String NIGHT_MODE = "NIGHT_MODE";
        String CONTEXT_INIT_DURATION = "CONTEXT_INIT_DURATION";
    }

    private static String getZenModeLabel(Context context) {
        int zenMode = ContextUtils.getZenMode(context);
        switch (zenMode) {
            case -3:
                return "N/A";
            case 0:
                return "off";
            case 1:
                return "priority_Only";
            case 2:
                return "total_silence";
            case 3:
                return "alarms_only";
            default:
                return "other_" + zenMode;
        }
    }

    public static String getNightMode(Context context) {
        int nightModeFlags = context.getResources()
                .getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                return "NIGHT_MODE_ON";
            case Configuration.UI_MODE_NIGHT_NO:
                return "NIGHT_MODE_OFF";
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return "NIGHT_MODE_UNDEFINED";
            default:
                return "NIGHT_MODE_UNKNOWN";
        }
    }

    @Override
    @NonNull
    public String toString() {
        return "ContextInitLogger{" +
                "timestamp='" + timestamp + '\'' +
                '}';
    }

    @HiltWorker
    public static class PongWorker extends Worker {

        public static final String NAME = "pong_worker";
        private final Lazy<CoreAPI> coreAPI;

        @AssistedInject
        public PongWorker(@Assisted @NotNull Context context,
                          @Assisted @NotNull WorkerParameters workerParams,
                          Lazy<CoreAPI> coreAPI) {
            super(context, workerParams);
            this.coreAPI = coreAPI;
        }

        @NonNull
        @Override
        public Result doWork() {
            Teller.logWorkerSession(getInputData());
            try {
                final Payload payload = new Payload();
                payload.setData(StringUtil.toStringMap(getAllUserProperties()));
                final Call<ServerResponse> call = coreAPI.get().pong(payload);
                final Response<ServerResponse> response = call.execute();
                return response.isSuccessful() ? Result.success() : Result.failure();
            } catch (IOException e) {
                Teller.warn("pong failed", e);
                return Result.failure();
            }
        }
    }
}
