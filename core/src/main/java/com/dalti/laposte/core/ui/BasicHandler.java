package com.dalti.laposte.core.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.annotation.WorkerThread;
import androidx.hilt.work.HiltWorker;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dalti.laposte.R;
import com.dalti.laposte.core.repositories.ActivationState;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.backend.LiveDataWrapper;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.StatefulActivity;
import dz.jsoftware95.silverbox.android.observers.ObserversUtil;

@MainThread
public class BasicHandler {

    private static final String SOURCE = "activation_banner";
    private static MutableLiveData<Integer> NETWORK_INDICATOR_VISIBILITY_INPUT;
    private static LiveDataWrapper<Integer> NETWORK_INDICATOR_VISIBILITY_OUTPUT;
    private static int BANNER_DELAY;

    private final LiveData<BannerState> bannerState;
    private final StatefulActivity activity;

    private final String activationTitle;
    private final Drawable activationIcon;

    public BasicHandler(StatefulActivity activity) {
        init();
        this.activity = Objects.requireNonNull(activity);
        this.bannerState = ObserversUtil.mapDelayed(getActivationState(), BANNER_DELAY,
                state -> new BannerState(activity, state));

        AbstractQueueApplication application = AbstractQueueApplication.requireInstance();
        activationTitle = application.getString(application.getActivationActivityTitle());
        activationIcon = VectorDrawableUtil.getDrawable(application, application.getActivationActivityIcon());
    }

    private static void init() {
        Assert.isMainThread();
        if (NETWORK_INDICATOR_VISIBILITY_OUTPUT == null) {
            AppConfig appConfig = AppConfig.getInstance();
            NETWORK_INDICATOR_VISIBILITY_INPUT = new MutableLiveData<>(ContextUtils.VIEW_INVISIBLE);
            NETWORK_INDICATOR_VISIBILITY_OUTPUT = new LiveDataWrapper<>(NETWORK_INDICATOR_VISIBILITY_INPUT, appConfig.getRemoteInt(LongSetting.NETWORK_INDICATOR_DELAY));
            BANNER_DELAY = appConfig.getRemoteInt(LongSetting.BANNER_DELAY);
        }
    }

    public static void showNetworkIndicator() {
        MutableLiveData<Integer> data = NETWORK_INDICATOR_VISIBILITY_INPUT;
        if (data != null)
            data.postValue(ContextUtils.VIEW_VISIBLE);
    }

    public static void hideNetworkIndicator() {
        MutableLiveData<Integer> data = NETWORK_INDICATOR_VISIBILITY_INPUT;
        if (data != null)
            data.postValue(ContextUtils.VIEW_INVISIBLE);
    }

    public String activationTitle() {
        return activationTitle;
    }

    public Drawable activationIcon() {
        return activationIcon;
    }

    public LiveData<ActivationState> getActivationState() {
        return AppConfig.getInstance().getActivationStateLiveData();
    }

    public LiveData<BannerState> getBannerState() {
        return bannerState;
    }

    public LiveData<Integer> getNetworkIndicatorVisibility() {
        return NETWORK_INDICATOR_VISIBILITY_OUTPUT.getLiveData();
    }

    public void openActivationActivity(View v) {
        AbstractQueueApplication application = AbstractQueueApplication.getInstance();
        if (application != null) {
            Intent intent = new Intent(activity, application.getActivationActivity());
            intent.putExtra(Teller.ACTIVATION_SOURCE, SOURCE);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(intent);
        }
    }

    public void refresh(View v) {
        AbstractQueueApplication.enqueue(RefreshWorker.class, RefreshWorker.NAME);
    }

    @HiltWorker
    public static final class RefreshWorker extends Worker {

        private final DashboardRepository dashboardRepository;
        public static final String NAME = "refresh_worker";

        @AssistedInject
        public RefreshWorker(@Assisted @NotNull Context context,
                             @Assisted @NotNull WorkerParameters workerParams,
                             DashboardRepository dashboardRepository) {
            super(context, workerParams);
            this.dashboardRepository = dashboardRepository;
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Teller.logWorkerSession(getInputData());
            dashboardRepository.refresh(true);
            return Result.success();
        }
    }

    @WorkerThread
    public static final class BannerState {

        private final StatefulActivity activity;
        private final Banner banner;
        private final Class<? extends Activity> infoActivity;

        public BannerState(StatefulActivity activity, ActivationState state) {
            Banner banner = Banner.from(activity, state);

            if (banner.activationNeeded)
                infoActivity = AbstractQueueApplication.requireInstance().getActivationInfoActivity();
            else if (banner.googleServicesError)
                infoActivity = HowToFixGoogleServicesActivity.class;
            else
                infoActivity = null;

            this.banner = banner;
            this.activity = Objects.requireNonNull(activity);
        }

        public Integer getVisibility() {
            return banner != Banner.NONE ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
        }

        public Integer getIcon() {
            return banner.icon;
        }

        public Integer getIconColor() {
            return banner.iconColor;
        }

        public String getText() {
            return banner.text;
        }

        public String getErrorCode() {
            final ConnectionResult connectionResult = banner.connectionResult;
            return connectionResult == null ? GlobalConf.EMPTY_TOKEN : String.valueOf(connectionResult.getErrorCode());
        }

        public Integer getInfoVisibility() {
            return infoActivity != null ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
        }

        public void openInfo() {
            if (infoActivity != null)
                activity.startActivity(infoActivity);
            else
                Teller.logUnexpectedCondition();
        }

        public Integer getActivationWarningVisibility() {
            return banner.activationNeeded ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
        }

        public Integer getSyncWarningVisibility() {
            return banner.syncNeeded ? ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
        }

        public Integer getResolutionVisibility() {
            return hasResolution() ?
                    ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
        }

        public Integer getOpenStoreVisibility() {
            return banner.googleServicesError && !hasResolution() ?
                    ContextUtils.VIEW_VISIBLE : ContextUtils.VIEW_GONE;
        }

        public Integer getRestartVisibility() {
            return ContextUtils.VIEW_GONE;
        }

        private boolean hasResolution() {
            return banner.connectionResult != null && banner.connectionResult.hasResolution();
        }

        public void openGoogleService(View v) {
            final ConnectionResult connectionResult = banner.connectionResult;
            if (connectionResult != null && connectionResult.getErrorCode() == ConnectionResult.SERVICE_DISABLED)
                ContextUtils.openAppSettings(activity, ContextUtils.GOOGLE_SERVICES_PACKAGE_NAME);
            else
                QueueUtils.openGoogleServicesInPlayStore(activity);
        }

        public void restart(View v) {
            if (v != null) {
                v.setEnabled(false);
                QueueUtils.restart();
            }
        }

        public void openResolutionActivity(View v) {
            final ConnectionResult connectionResult = banner.connectionResult;
            if (connectionResult != null) {
                try {
                    connectionResult.startResolutionForResult(activity, Request.GOOGLE_SERVICES_RESOLUTION.ordinal());
                } catch (IntentSender.SendIntentException e) {
                    Teller.logUnexpectedCondition("error code: " + connectionResult.getErrorCode());
                    openGoogleService(v);
                }
            }
        }

        static class Banner {
            static Banner NONE = new Banner(null, null, null, false, false);

            final Integer icon;
            final Integer iconColor;
            final String text;
            final ConnectionResult connectionResult;

            final boolean activationNeeded;
            final boolean syncNeeded;
            final boolean googleServicesError;

            public Banner(Integer icon, Integer iconColor, String text, boolean activationNeeded, boolean syncNeeded) {
                this(icon, iconColor, text, activationNeeded, syncNeeded, false, null);
            }

            Banner(Integer icon, Integer iconColor, String text,
                   boolean activationNeeded, boolean syncNeeded,
                   boolean googleServicesError, ConnectionResult connectionResult) {
                this.icon = icon;
                this.iconColor = iconColor;
                this.text = text;
                this.activationNeeded = activationNeeded;
                this.syncNeeded = syncNeeded;
                this.googleServicesError = googleServicesError;
                this.connectionResult = connectionResult;
            }

            private static Banner newGoogleServicesBanner(int iconColor, String text, boolean googleServicesError, ConnectionResult connectionResult) {
                return new Banner(R.drawable.ic_baseline_signal_wifi_off_24, iconColor, text, false, false, googleServicesError, connectionResult);
            }

            private static Banner newGoogleServicesErrorBanner(Context context, boolean fatalError) {
                final GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
                final int connectionResultCode = availability.isGooglePlayServicesAvailable(context);
                final ConnectionResult connectionResult = new ConnectionResult(connectionResultCode);
                final boolean hasResolution = connectionResult.hasResolution();
                if (fatalError) {
                    if (connectionResultCode == ConnectionResult.SUCCESS) {
                        final String text = context.getString(R.string.waiting_for_google_services);
                        return newGoogleServicesBanner(R.color.primary_color_selector, text, true, null);
                    } else {
                        final String text = getText(context, connectionResultCode, R.string.google_services_error_code);
                        return newGoogleServicesBanner(R.color.error_on_foreground_color_selector, text, true, connectionResult);
                    }
                } else if (connectionResultCode == ConnectionResult.SUCCESS)
                    return NONE;
                else {
                    final String text = getText(context, connectionResultCode, R.string.google_services_problem_code);
                    return newGoogleServicesBanner(R.color.warning_on_foreground_color_selector, text, true, connectionResult);
                }
            }

            private static String getText(Context context, int connectionResultCode, @StringRes int msg) {
                if (connectionResultCode == ConnectionResult.SERVICE_MISSING)
                    return context.getString(R.string.google_services_missing);
                else if (connectionResultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
                    return context.getString(R.string.google_services_need_update);
                else if (connectionResultCode == ConnectionResult.SERVICE_DISABLED)
                    return context.getString(R.string.google_services_disabled);
                else
                    return context.getString(msg, connectionResultCode);
            }

            @NonNull
            static Banner from(StatefulActivity activity, ActivationState state) {
                AppConfig appConfig = AppConfig.findInstance();
                if (GlobalUtil.isBlankToken(state.getApplicationID())) {
                    if (appConfig != null && appConfig.isWaitingForAppID()) {
                        final String text = activity.getString(R.string.waiting_for_google_services);
                        return newGoogleServicesBanner(R.color.primary_color_selector, text, false, null);
                    } else
                        return newGoogleServicesErrorBanner(activity, true);
                } else if (state.isNotActive())
                    return new Banner(R.drawable.ic_baseline_warning_24, R.color.warning_on_foreground_color_selector, activity.getString(R.string.application_not_activated), true, false);
                else if (state.isSyncNeeded() && state.isActive())
                    return new Banner(R.drawable.ic_baseline_signal_wifi_off_24, R.color.warning_on_foreground_color_selector, activity.getString(R.string.connection_error), false, true);
                else
                    return newGoogleServicesErrorBanner(activity, false);
            }
        }
    }

    public String getNamespace() {
        return "basic_handler";
    }
}
