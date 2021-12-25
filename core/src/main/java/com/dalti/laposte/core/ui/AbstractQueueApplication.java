package com.dalti.laposte.core.ui;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.hilt.work.HiltWorker;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dalti.laposte.R;
import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.repositories.AppConfig;
import com.dalti.laposte.core.repositories.DashboardRepository;
import com.dalti.laposte.core.repositories.LongSetting;
import com.dalti.laposte.core.repositories.Teller;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueConfig;
import com.dalti.laposte.core.util.QueueUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.JsonUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.middleware.StatefulApplication;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;
import okhttp3.OkHttpClient;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public abstract class AbstractQueueApplication extends StatefulApplication implements Configuration.Provider {

    public static final String NETWORK_JOB = "NETWORK_JOB";
    public static final String UNIQUE_JOB = "UNIQUE_JOB";
    public static final Constraints NETWORK_JOB_CONSTRAINTS = new Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build();

    private static volatile AbstractQueueApplication CURRENT;
    private static final AtomicInteger activationNeededEncounters = new AtomicInteger();
    private static final AtomicInteger updateNeededEncounters = new AtomicInteger();

    private final WeakHashMap<Runnable, Long> routines = new WeakHashMap<>();
    private final CountDownLatch initLatch = new CountDownLatch(1);

    @Inject
    HiltWorkerFactory workerFactory;

    @Inject
    BuildConfiguration buildConfiguration;

    @Inject
    Teller teller;

    {
        CURRENT = this;
    }

    @NonNull
    public static AbstractQueueApplication requireInstance() {
        return Objects.requireNonNull(CURRENT);
    }

    @Nullable
    public static AbstractQueueApplication getInstance() {
        return CURRENT;
    }

    public abstract FirebaseApp getFirebaseApp();

    public abstract FirebaseAuth getFirebaseAuth();

    @Nullable
    public static FirebaseAnalytics getAnalytics() {
        AbstractQueueApplication app = getInstance();
        if (app != null)
            return FirebaseAnalytics.getInstance(app);
        else
            return null;
    }

    public static AtomicInteger getActivationNeededEncounters() {
        return activationNeededEncounters;
    }

    public static AtomicInteger getUpdateNeededEncounters() {
        return updateNeededEncounters;
    }

    public static void enqueue(@NonNull Class<? extends Worker> workerClass, String workerName) {
        getWorkManager().enqueue(new OneTimeWorkRequest.Builder(workerClass)
                .setInputData(Teller.logWorkerRequest(workerName).build())
                .build());
    }

    public void addRoutine(Runnable runnable) {
        Objects.requireNonNull(runnable);
        AppWorker.BACKGROUND.execute(() -> {
            synchronized (routines) {
                routines.put(runnable, System.currentTimeMillis());
            }
        });
    }

    @AnyThread
    public abstract Class<? extends AbstractQueueActivity> getMainActivity();

    @AnyThread
    public abstract Class<? extends BasicActionReceiver> getMainActionReceiver();

    @AnyThread
    public abstract Class<? extends AbstractQueueActivity> getActivationInfoActivity();

    @AnyThread
    public abstract Class<? extends AbstractQueueActivity> getActivationActivity();

    @AnyThread
    public abstract Class<? extends AbstractQueueActivity> getAboutUsActivity();

    @AnyThread
    public abstract Class<? extends AbstractQueueActivity> getHelpActivity();

    @AnyThread
    public abstract Class<? extends AbstractQueueActivity> getPrivacyPolicyActivity();

    @AnyThread
    public abstract Integer getActivationActivityTitle();

    @AnyThread
    public abstract Integer getActivationActivityIcon();

    @AnyThread
    public Intent newActionReceiverIntent(String action) {
        Intent intent = new Intent(this, getMainActionReceiver());
        intent.putExtra(BasicActionReceiver.ACTION_KEY, action);
        return intent;
    }

    @AnyThread
    public static BuildConfiguration getCurrentBuildConfiguration() {
        AbstractQueueApplication app = CURRENT;
        return app != null ? app.buildConfiguration : null;
    }

    @Override
    public void onCreate() {
        initFirebaseApp();
        AppConfig.init(this);
        super.onCreate();
        setTheme(R.style.Theme_App_Basic);
        setupCrashActivity();
        createNotificationChannels();
        initBackgroundWorkers();
        initLatch.countDown();
    }

    @WorkerThread
    public void waitInit() throws InterruptedException {
        initLatch.await(3, TimeUnit.SECONDS);
    }

    private void setupCrashActivity() {
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT)
                .errorDrawable(R.drawable.ic_logo_crash_240)
                .showErrorDetails(QueueUtils.isTesting())
                .trackActivities(true)
                .apply();
    }

    public void initBackgroundWorkers() {
        AppWorker.BACKGROUND.executeDelayed(newRoutineJob(routines), TimeUtils.ONE_MINUTE_MILLIS);
        getWorkManager().enqueueUniquePeriodicWork(PeriodicSynchronizer.NAME, ExistingPeriodicWorkPolicy.KEEP,
                new PeriodicWorkRequest.Builder(PeriodicSynchronizer.class, 15, TimeUnit.MINUTES)
                        .setInitialDelay(PeriodicSynchronizer.MAX_IDLENESS_MILLIS + TimeUtils.ONE_MINUTE_MILLIS, TimeUnit.MILLISECONDS)
                        .setConstraints(NETWORK_JOB_CONSTRAINTS)
                        .setInputData(Teller.logWorkerRequest(PeriodicSynchronizer.NAME, false).build())
                        .build()
        );
    }

    protected abstract void initFirebaseApp();

    private static Runnable newRoutineJob(Map<Runnable, Long> jobs) {
        return new Runnable() {
            @Override
            public void run() {
                synchronized (jobs) {
                    for (Runnable runnable : jobs.keySet())
                        if (runnable != null)
                            runnable.run();
                }
                AppWorker.BACKGROUND.executeDelayed(this, TimeUtils.ONE_MINUTE_MILLIS);
            }
        };
    }

    protected void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                addAlarmsChannel(notificationManager);
                addInformationChannel(notificationManager);
                addOnGoingOperationsChannel(notificationManager);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void addAlarmsChannel(NotificationManager notificationManager) {
        CharSequence name = getString(R.string.alarms_channel_name);
        String description = getString(R.string.alarms_channel_description);
        String channelID = getString(R.string.alarms_channel_id);

        NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(description);
        channel.setShowBadge(true);
        channel.setVibrationPattern(QueueConfig.ALARM_VIBRATION_PATTERN);

        notificationManager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void addInformationChannel(NotificationManager notificationManager) {
        CharSequence name = getString(R.string.info_channel_name);
        String description = getString(R.string.info_channel_description);
        String channelID = getString(R.string.info_channel_id);

        NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(description);
        channel.setShowBadge(false);
        channel.setVibrationPattern(QueueConfig.INFO_VIBRATION_PATTERN);

        notificationManager.createNotificationChannel(channel);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void addOnGoingOperationsChannel(NotificationManager notificationManager) {
        CharSequence name = getString(R.string.ongoing_operations_channel_name);
        String description = getString(R.string.ongoing_notifications_channel_description);
        String channelID = getString(R.string.ongoing_operations_channel_id);

        NotificationChannel channel = new NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_LOW);
        channel.setDescription(description);
        channel.setShowBadge(false);
        channel.setVibrationPattern(null);

        notificationManager.createNotificationChannel(channel);
    }

    public static <T> T parse(String json, Class<T> type) throws JsonProcessingException {
        return JsonUtil.getJacksonMapper().readValue(json, type);
    }

    @NonNull
    public static Converter.Factory buildConverterFactory() {
        return JacksonConverterFactory.create(JsonUtil.getJacksonMapper());
    }

    @NotNull
    public static Retrofit buildRetrofitClient() {
        final AppConfig appConfig = AppConfig.getInstance();
        return new Retrofit.Builder()
                .baseUrl(appConfig.getCoreApiUrl())
//                .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                .client(new OkHttpClient.Builder()
                        .connectTimeout(appConfig.getRemoteLong(LongSetting.CONNECT_TIMEOUT_IN_SECONDS), TimeUnit.SECONDS)
                        .readTimeout(appConfig.getRemoteLong(LongSetting.READ_TIMEOUT_IN_SECONDS), TimeUnit.SECONDS)
                        .writeTimeout(appConfig.getRemoteLong(LongSetting.WRITE_TIMEOUT_IN_SECONDS), TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(buildConverterFactory())
                .build();
    }

    @NotNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .setExecutor(AppWorker.SYNC)
                .setWorkerFactory(Objects.requireNonNull(workerFactory))
                .build();
    }

    public static WorkManager getWorkManager() {
        return WorkManager.getInstance(requireInstance());
    }

    public static void enqueueUniqueNetworkJob(String workName, Class<? extends Worker> workerClass, Data data) {
        getWorkManager().enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, newNetworkRequestBuilder(workerClass, data)
                .addTag(UNIQUE_JOB)
                .build());
    }

    private static OneTimeWorkRequest.Builder newNetworkRequestBuilder(Class<? extends Worker> workerClass, Data data) {
        OneTimeWorkRequest.Builder workerBuilder = new OneTimeWorkRequest.Builder(workerClass)
                .addTag(NETWORK_JOB)
                .setConstraints(NETWORK_JOB_CONSTRAINTS);

        if (data != null)
            workerBuilder.setInputData(data);

        return workerBuilder;
    }

    public static void cancelWork(String workName, long duration, @NonNull TimeUnit unit) {
        if (workName != null) {
            OneTimeWorkRequest.Builder requestBuilder = new OneTimeWorkRequest.Builder(WorkCanceler.class)
                    .setInputData(Teller.logWorkerRequest(WorkCanceler.NAME)
                            .putString(WorkCanceler.KEY_WORK_NAME, workName)
                            .build())
                    .setInitialDelay(duration, unit);
            getWorkManager().enqueue(requestBuilder.build());
        }
    }

    public static void cancelWork(String workName) {
        if (workName != null)
            getWorkManager().enqueue(
                    new OneTimeWorkRequest.Builder(WorkCanceler.class)
                            .setInputData(new Data.Builder().putString(WorkCanceler.KEY_WORK_NAME, workName).build())
                            .build()
            );
    }

    @AnyThread
    public abstract Long getInitDuration();

    public static class WorkCanceler extends Worker {

        public static final String NAME = "work_canceler";
        public static final String KEY_WORK_NAME = "KEY_WORK_NAME";

        public WorkCanceler(@NotNull Context context,
                            @NotNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            Data data = getInputData();
            Teller.logWorkerSession(data);
            String workName = data.getString(KEY_WORK_NAME);
            if (workName != null)
                try {
                    WorkManager.getInstance(getApplicationContext()).cancelUniqueWork(workName);
                } catch (RuntimeException e) {
                    Teller.warn("could not cancel scheduled work: " + workName, e);
                }
            return Result.success();
        }
    }

    @HiltWorker
    public static class PeriodicSynchronizer extends Worker {

        public static final long MAX_IDLENESS_MILLIS = TimeUnit.MINUTES.toMillis(5);
        public static final String NAME = "periodic_synchronizer";
        private final long ttlWithoutUserInteraction;

        private final Context context;
        private final DashboardRepository dashboardRepository;


        @AssistedInject
        public PeriodicSynchronizer(@Assisted @NotNull Context context,
                                    @Assisted @NotNull WorkerParameters workerParams,
                                    DashboardRepository dashboardRepository) {
            super(context, workerParams);
            this.context = context;
            this.dashboardRepository = dashboardRepository;
            this.ttlWithoutUserInteraction = AppConfig.getInstance().getRemoteLong(LongSetting.TTL_WITHOUT_USER_INTERACTION);
        }

        @NonNull
        @NotNull
        @Override
        public Result doWork() {
            try {
                Teller.logWorkerSession(getInputData());
                AppConfig appConfig = AppConfig.getInstance();
                refresh(appConfig);
                Teller.uploadLoggedEventsNow();
                considerCanceling(appConfig);
            } catch (Exception e) {
                Teller.warn("error while refreshing from the background", e);
            }
            return Result.success();
        }

        public void refresh(AppConfig appConfig) throws InterruptedException {
            long now = System.currentTimeMillis();
            appConfig.put(LongSetting.LAST_AUTO_REFRESH, now);
            if (appConfig.getActivationState().isSyncNeeded() ||
                    now - appConfig.getLastUpdate() > MAX_IDLENESS_MILLIS)
                dashboardRepository.refreshAndWait();
        }

        public void considerCanceling(AppConfig appConfig) {
            long now = System.currentTimeMillis();
            if (appConfig.uptime(now) > TimeUtils.ONE_MINUTE_MILLIS
                    && now - appConfig.getLastUserInteraction() > ttlWithoutUserInteraction)
                WorkManager.getInstance(context).cancelWorkById(getId());
        }
    }

    @AnyThread
    public static String getCurrentAppPrefix() {
        BuildConfiguration buildConf = getCurrentBuildConfiguration();
        if (buildConf == null)
            return "null_";
        else if (buildConf.isClient())
            return "client_";
        else if (buildConf.isAdmin())
            return "admin_";
        else
            return "app_";
    }

    @AnyThread
    public static String getCurrentVersionName() {
        BuildConfiguration buildConf = getCurrentBuildConfiguration();
        return buildConf != null ? buildConf.getFullVersionName() : GlobalConf.EMPTY_TOKEN;
    }

    @AnyThread
    public static String getCurrentHttpUserAgent() {
        return "dalti-laposte-" + getCurrentAppPrefix() + getCurrentVersionName();
    }
}
