package com.dalti.laposte.core.repositories;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.dalti.laposte.core.ui.Named;
import com.dalti.laposte.core.util.BuildConfiguration;
import com.dalti.laposte.core.util.QueueUtils;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.silverbox.android.common.StringUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.Mutex;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.middleware.ContextUtils;
import dz.jsoftware95.silverbox.android.middleware.TimeUtils;

@AnyThread
public class AppConfig {

    public static final String SYSTEM_PREFERENCES_NAME = "com.dalti.laposte.system_preferences";

    private static final String NO_CONFIG_CACHE = "NO_CONFIG_CACHE";
    private static final String SCHEMA_VERSION = "SCHEMA_VERSION";
    private static final String ACTIVATION_KEY = "ACTIVATION_KEY";
    private static final String ACTIVATION_EXPIRATION_DATE = "ACTIVATION_DEADLINE";
    private static final String APPLICATION_ID = "APPLICATION_ID";
    private static final String IN_APP_MESSAGE_ID = "IN_APP_MESSAGE_ID";
    private static final String SERVER_SITUATION = "SERVER_SITUATION";
    private static final String LAST_UPDATE = "LAST_UPDATE";
    private static final String CLIENT_SITUATION = "CLIENT_SITUATION";
    private static final String FIRST_STARTUP_KEY = "FIRST_STARTUP";
    private static final String COMPACT_DASHBOARD_SHOWN_KEY = "COMPACT_UI_SHOWN";

    public static final long DEFAULT_SERVER_SITUATION = 0;
    public static final long DEFAULT_CLIENT_SITUATION = 1;
    public static final int NULL_SCHEMA = -1;
    private static final String SYNCED = "_SYNCED";

    private static volatile AppConfig INSTANCE;
    private static final Object mutex = new Mutex();

    protected final long initTime = System.currentTimeMillis();
    protected final FirebaseRemoteConfig remoteConfig;
    protected final SharedPreferences userPreferences;
    protected final SharedPreferences systemPreferences;
    protected final MutableLiveData<ActivationState> activationStateLiveData;
    protected final MutableLiveData<Boolean> compactDashboardShown;
    protected final AtomicLong lastUserInteraction = new AtomicLong();

    @MainThread
    public static void init(AbstractQueueApplication context) {
        if (INSTANCE == null)
            synchronized (mutex) {
                if (INSTANCE == null)
                    INSTANCE = new AppConfig(context);
            }
    }

    @NonNull
    public static AppConfig getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }

    @Nullable
    public static AppConfig findInstance() {
        return INSTANCE;
    }

    @WorkerThread
    public static void setNoConfigCache(boolean value) {
        AbstractQueueApplication.requireInstance()
                .getSharedPreferences(AppConfig.SYSTEM_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(NO_CONFIG_CACHE, value)
                .apply();
    }

    @MainThread
    public AppConfig(AbstractQueueApplication context) {
        remoteConfig = FirebaseRemoteConfig.getInstance(context.getFirebaseApp());
        userPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        systemPreferences = getSystemPreferences(context);
        activationStateLiveData = new MutableLiveData<>(getActivationState());
        compactDashboardShown = new MutableLiveData<>(isCompactDashboardShown());

        new UpdateCompactDashboardStateJob(context, compactDashboardShown).execute();
        activate();

        setupInAppId();
    }

    public void setupInAppId() {
        try {
            FirebaseInstallations firebaseInstallations = FirebaseInstallations.getInstance();
            firebaseInstallations.getId().addOnCompleteListener(task -> {
                String newID = task.isSuccessful() ? task.getResult() : GlobalConf.EMPTY_TOKEN;
                Teller.logAppConfig(IN_APP_MESSAGE_ID, newID);
                systemPreferences.edit().putString(IN_APP_MESSAGE_ID, newID).apply();
            });
        } catch (RuntimeException e) {
            Teller.warn("could not resister in app id listener", e);
        }
    }

    public MutableLiveData<Boolean> getCompactDashboardShown() {
        return compactDashboardShown;
    }

    public boolean isCompactDashboardShown() {
        return systemPreferences.getBoolean(COMPACT_DASHBOARD_SHOWN_KEY, false);
    }

    public void setCompactDashboardShown(boolean isShown) {
        systemPreferences.edit().putBoolean(COMPACT_DASHBOARD_SHOWN_KEY, isShown).apply();
        Teller.logAppConfig(COMPACT_DASHBOARD_SHOWN_KEY, isShown);
        compactDashboardShown.postValue(isShown);
    }

    @NotNull
    public static SharedPreferences getSystemPreferences(Application context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    SYSTEM_PREFERENCES_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (RuntimeException | GeneralSecurityException | IOException e) {
            Teller.error("could not create encrypted shared preferences", e);
            return context.getSharedPreferences(SYSTEM_PREFERENCES_NAME, Context.MODE_PRIVATE);
        }
    }

    public void activate() {
        remoteConfig.activate().addOnCompleteListener(task -> fetchRemoteConfig());
    }

    private void fetchRemoteConfig() {
        final boolean noCache = systemPreferences.getBoolean(NO_CONFIG_CACHE, false);
        if (noCache || QueueUtils.isTesting())
            remoteConfig.fetch(0).addOnCompleteListener(fetch -> {
                if (fetch.isSuccessful())
                    systemPreferences.edit().putBoolean(NO_CONFIG_CACHE, false).apply();
            });
        else
            remoteConfig.fetch();
    }

    public Integer getSchemaVersion() {
        return systemPreferences.getInt(SCHEMA_VERSION, NULL_SCHEMA);
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void setSchemaVersion(int version) {
        systemPreferences.edit().putInt(SCHEMA_VERSION, version).commit();
        Teller.logAppConfig(SCHEMA_VERSION, version);
    }

    @NonNull
    public String getCoreApiUrl() {
        if (QueueUtils.isTesting()) {
            if (ContextUtils.isEmulator())
                return BuildConfiguration.getEmulatorLocalServicesApiUrl();
            else
                return BuildConfiguration.getLocalServicesApiUrl();
        } else
            return getRemoteString(StringSetting.SERVICES_API_URL);
    }

    public String getRemoteString(@NonNull StringPreference preference) {
        Objects.requireNonNull(preference);
        try {
            FirebaseRemoteConfigValue remoteValue = remoteConfig.getValue(preference.name());
            if (remoteValue.getSource() == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE)
                return remoteValue.asString();
        } catch (RuntimeException e) {
            Teller.warn("cannot load remote string: " + preference);
        }

        return preference.getDefaultString();
    }

    public long getRemoteLong(@NonNull LongPreference preference) {
        Objects.requireNonNull(preference);
        try {
            FirebaseRemoteConfigValue remoteValue = remoteConfig.getValue(preference.name());
            if (remoteValue.getSource() == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE)
                return remoteValue.asLong();
        } catch (RuntimeException e) {
            Teller.warn("cannot load remote long: " + preference);
        }

        return preference.getDefaultLong();
    }

    public boolean getRemoteBoolean(@NonNull BooleanPreference preference) {
        Objects.requireNonNull(preference);
        try {
            FirebaseRemoteConfigValue remoteValue = remoteConfig.getValue(preference.name());
            if (remoteValue.getSource() == FirebaseRemoteConfig.VALUE_SOURCE_REMOTE)
                return remoteValue.asBoolean();
        } catch (RuntimeException e) {
            Teller.warn("cannot load remote boolean: " + preference, e);
        }

        return preference.getDefaultBoolean();
    }

    public int getRemoteInt(@NonNull LongPreference preference) {
        return StringUtil.cast(getRemoteLong(preference));
    }

    public String get(@NonNull StringPreference preference) {
        return userPreferences.getString(preference.name(), preference.getDefaultString());
    }

    public String getString(String key) {
        return userPreferences.getString(key, "");
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    private long commitLongHelper(String key, Long value, SharedPreferences preferences, long defaultValue) throws LocalPersistentException {
        long longValue = value != null ? value : defaultValue;
        if (preferences.edit().putLong(key, longValue).commit())
            Teller.logAppConfig(key, value);
        else
            Teller.handleFailedAppConfig(key, value);
        return longValue;
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void commit(@NonNull LongPreference preference, @Nullable Long value) throws LocalPersistentException {
        commitLongHelper(preference.name(), value, userPreferences, preference.getDefaultLong());
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void commit(@NonNull StringPreference preference, @Nullable String value) throws LocalPersistentException {
        String key = preference.name();
        if (userPreferences.edit().putString(key, value != null ? value : preference.getDefaultString()).commit())
            Teller.logAppConfig(key, value);
        else
            Teller.handleFailedAppConfig(key, value);
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void commit(@NonNull BooleanPreference preference, @Nullable Boolean value) throws LocalPersistentException {
        String key = preference.name();
        if (userPreferences.edit().putBoolean(key, value != null ? value : preference.getDefaultBoolean()).commit())
            Teller.logAppConfig(key, value);
        else
            Teller.handleFailedAppConfig(key, value);
    }

    public void put(@NonNull LongPreference preference, @Nullable Integer value) {
        putLongHelper(preference, value != null ? value.longValue() : preference.getDefaultLong());
    }

    public void put(@NonNull LongPreference preference, @Nullable Long value) {
        putLongHelper(preference, value != null ? value : preference.getDefaultLong());
    }

    private void putLongHelper(@NotNull LongPreference preference, long newValue) {
        String key = preference.name();
        userPreferences.edit().putLong(key, newValue).apply();
        Teller.logAppConfig(key, newValue);
    }

    public void put(@NonNull StringPreference preference, @Nullable String value) {
        String key = preference.name();
        String newValue = value != null ? value : preference.getDefaultString();
        userPreferences.edit().putString(key, newValue).apply();
        Teller.logAppConfig(key, newValue);
    }

    public void put(@NonNull BooleanPreference preference, @Nullable Boolean value) {
        String key = preference.name();
        boolean newValue = value != null ? value : preference.getDefaultBoolean();
        userPreferences.edit().putBoolean(key, newValue).apply();
        Teller.logAppConfig(key, newValue);
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void removeCommit(@NonNull Named preference) {
        userPreferences.edit().remove(preference.name()).commit();
    }

    public void remove(@NonNull Named preference) {
        userPreferences.edit().remove(preference.name()).apply();
    }

    public Map<String, String> getAll() {
        Map<String, ?> userData = userPreferences.getAll();
        Map<String, ?> systemData = systemPreferences.getAll();
        HashMap<String, String> allData = new HashMap<>(userData.size() + systemData.size());
        for (Map.Entry<String, ?> entry : userData.entrySet())
            allData.put(entry.getKey(), StringUtil.toString(entry.getValue()));
        for (Map.Entry<String, ?> entry : systemData.entrySet())
            allData.put(entry.getKey(), StringUtil.toString(entry.getValue()));
        return allData;
    }

    public void addListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        userPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public long getLong(@NonNull LongPreference preference) {
        return userPreferences.getLong(preference.name(), preference.getDefaultLong());
    }

    public int getInt(@NonNull IntegerPreference preference) {
        return userPreferences.getInt(preference.name(), preference.getDefaultInteger());
    }

    public int getAsInt(@NonNull LongPreference preference) {
        return StringUtil.cast(getLong(preference));
    }

    public boolean get(@NonNull BooleanPreference preference) {
        return userPreferences.getBoolean(preference.name(), preference.getDefaultBoolean());
    }

    public <T extends StringPreference & LongPreference> long getAsLong(@NonNull T preference) {
        return StringUtil.parseLong(userPreferences.getString(preference.name(), preference.getDefaultString()), preference.getDefaultLong());
    }

    public <T extends StringPreference & LongPreference> int getAsInt(@NonNull T preference) {
        return StringUtil.cast(getAsLong(preference));
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void setApplicationID(@NonNull String idToken, @NotNull BuildConfiguration config) {
        systemPreferences.edit().putString(APPLICATION_ID, idToken).commit();
        Teller.logAppConfig(APPLICATION_ID, idToken);
        setUserID(idToken);
        setServerSituation(null);
    }

    public void setUserID(@NotNull String idToken) {
        AbstractQueueApplication application = AbstractQueueApplication.getInstance();
        if (application != null) {
            String userId = AbstractQueueApplication.getCurrentAppPrefix() + idToken;

            FirebaseAnalytics analytics = FirebaseAnalytics.getInstance(application);
            //noinspection ConstantConditions
            if (analytics != null)
                analytics.setUserId(userId);

            FirebaseCrashlytics crashlytics = Teller.getCrashlytics();
            if (crashlytics != null)
                crashlytics.setUserId(userId);
        }
    }

    public String getInAppMessageID() {
        return systemPreferences.getString(IN_APP_MESSAGE_ID, GlobalConf.EMPTY_TOKEN);
    }

    @NonNull
    public String getApplicationID() {
        return systemPreferences.getString(APPLICATION_ID, GlobalConf.EMPTY_TOKEN);
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public boolean getAndSet(BooleanPreference preference, boolean newValue) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (preference) {
            boolean value = get(preference);
            if (value != newValue) {
                String key = preference.name() + SYNCED;
                userPreferences.edit().putBoolean(key, newValue).commit();
                Teller.logAppConfig(key, newValue);
            }
            return value;
        }
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void add(SetPreference preference, String value) {
        if (value != null)
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (preference) {
                String key = preference.name() + SYNCED;
                Set<String> oldSet = userPreferences.getStringSet(key, preference.getDefaultSet());
                HashSet<String> newSet = new HashSet<>(oldSet.size() + 1);
                newSet.addAll(oldSet);
                newSet.add(value);
                userPreferences.edit().putStringSet(key, newSet).commit();
                Teller.logAppConfig(key, newSet);
            }
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void remove(SetPreference preference, Collection<String> values) {
        if (values != null && !values.isEmpty())
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (preference) {
                String key = preference.name() + SYNCED;
                Set<String> oldSet = userPreferences.getStringSet(key, preference.getDefaultSet());
                HashSet<String> newSet = new HashSet<>(oldSet.size());
                newSet.addAll(oldSet);
                newSet.removeAll(values);
                userPreferences.edit().putStringSet(key, newSet).commit();
                Teller.logAppConfig(key, newSet);
            }
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public Set<String> getAndClear(SetPreference preference) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (preference) {
            Set<String> set = get(preference);
            String key = preference.name() + SYNCED;
            userPreferences.edit().remove(key).commit();
            Teller.logAppConfig(key, null);
            return set;
        }
    }

    public Set<String> get(SetPreference preference) {
        return userPreferences.getStringSet(preference.name() + SYNCED, preference.getDefaultSet());
    }

    private static final class UpdateCompactDashboardStateJob extends DuoJob<Context, MutableLiveData<Boolean>> {

        public UpdateCompactDashboardStateJob(@NonNull Context context1,
                                              @NonNull MutableLiveData<Boolean> context2) {
            super(AppWorker.BACKGROUND, context1, context2);
        }

        @Override
        protected void doFromBackground(@NonNull Context context, @NonNull MutableLiveData<Boolean> liveData) {
            liveData.postValue(ContextUtils.isServiceRunning(context, "com.dalti.laposte.admin.ui.CompactDashboardService"));
        }
    }

    public long getLastUserInteraction() {
        return lastUserInteraction.get();
    }

    public void updateLastUserInteraction() {
        lastUserInteraction.set(System.currentTimeMillis());
    }

    public long uptime(long time) {
        return Math.max(time - initTime, 0);
    }

    // activation state methods

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void setServerSituation(@Nullable Long value) {
        long serverSituation = value != null ? value : DEFAULT_SERVER_SITUATION;
        systemPreferences.edit().putLong(SERVER_SITUATION, serverSituation).commit();
        Teller.logAppConfig(SERVER_SITUATION, serverSituation);

        refreshActivationState();

        systemPreferences.edit().putLong(LAST_UPDATE, System.currentTimeMillis()).commit();
    }

    public long getServerSituation() {
        return systemPreferences.getLong(SERVER_SITUATION, DEFAULT_SERVER_SITUATION);
    }

    public long getLastUpdate() {
        return systemPreferences.getLong(LAST_UPDATE, 0);
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void setClientSituation(@Nullable Long value) {
        long clientSituation = value != null ? value : DEFAULT_CLIENT_SITUATION;
        systemPreferences.edit().putLong(CLIENT_SITUATION, clientSituation).commit();
        Teller.logAppConfig(CLIENT_SITUATION, clientSituation);

        refreshActivationState();
    }

    public long getClientSituation() {
        return systemPreferences.getLong(CLIENT_SITUATION, DEFAULT_CLIENT_SITUATION);
    }

    public ActivationState getActivationState() {
        return new ActivationState(getActivationKey(), getActivationExpDate(),
                getClientSituation(), getServerSituation(),
                getApplicationID(), QueueUtils.getGoogleServicesVersion());
    }

    public LiveData<ActivationState> getActivationStateLiveData() {
        return activationStateLiveData;
    }

    public long getActivationKey() {
        return systemPreferences.getLong(ACTIVATION_KEY, 0);
    }

    public long getActivationExpDate() {
        return systemPreferences.getLong(ACTIVATION_EXPIRATION_DATE, 0);
    }

    @WorkerThread
    public ActivationState resetActivation() throws LocalPersistentException {
        return updateActivationState(0L, 0L);
    }

    @WorkerThread
    public ActivationState updateActivationState(Long activationKey, Long expirationDate) throws LocalPersistentException {
        long key = commitLongHelper(ACTIVATION_KEY, activationKey, systemPreferences, 0);
        long date = commitLongHelper(ACTIVATION_EXPIRATION_DATE, expirationDate, systemPreferences, 0);
        return refreshActivationState();
    }

    public ActivationState refreshActivationState() {
        ActivationState state = getActivationState();
        activationStateLiveData.postValue(state);
        return state;
    }

    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public void updateActivationExpirationDate(Long expirationDate) throws LocalPersistentException {
        commitLongHelper(ACTIVATION_EXPIRATION_DATE, expirationDate, systemPreferences, 0);
        refreshActivationState();
    }

    public boolean isWaitingForAppID() {
        if (get(BooleanSetting.WAIT_APPLICATION_ID)) {
            long delayForAppId = getRemoteLong(LongSetting.APP_ID_DELAY);
            boolean shouldWait = uptime(System.currentTimeMillis()) < delayForAppId;
            if (shouldWait)
                newResetLaterJob(this, delayForAppId).executeDelayed(delayForAppId + TimeUtils.ONE_SECOND_MILLIS);
            return shouldWait;
        } else
            return false;
    }

    public static Job newResetLaterJob(AppConfig appConfig, long delayForAppId) {
        return new UnJob<AppConfig>(AppWorker.BACKGROUND, appConfig) {
            @Override
            protected void doFromBackground(@NonNull @NotNull AppConfig appConfig) {
                appConfig.commit(BooleanSetting.WAIT_APPLICATION_ID, false);
                appConfig.refreshActivationState();
            }
        };
    }

    public void logDelayedException(String msg) {
        systemPreferences.edit()
                .putString(DelayedException.KEY_MESSAGE, msg)
                .putLong(DelayedException.KEY_TIME, System.currentTimeMillis())
                .apply();
    }

    @Nullable
    @WorkerThread
    @SuppressLint("ApplySharedPref")
    public DelayedException getDelayedException() {
        String msg = systemPreferences.getString(DelayedException.KEY_MESSAGE, "");
        long time = systemPreferences.getLong(DelayedException.KEY_TIME, 0);
        systemPreferences.edit()
                .putString(DelayedException.KEY_MESSAGE, "")
                .putLong(DelayedException.KEY_TIME, 0)
                .commit();

        if (StringUtil.notBlank(msg))
            return new DelayedException(msg, time);
        else
            return null;
    }

    public static final class DelayedException {
        private static final String KEY_MESSAGE = "DELAYED_EXCEPTION_MESSAGE";
        private static final String KEY_TIME = "DELAYED_EXCEPTION_TIME";

        private final String message;
        private final Long time;

        public DelayedException(@NonNull String message, Long time) {
            this.message = Objects.requireNonNull(message);
            this.time = time != null && time > 0 ? time : null;
        }

        @NonNull
        public String getMessage() {
            return message;
        }

        @Nullable
        public Long getTime() {
            return time;
        }

        @Override
        public String toString() {
            return "DelayedException (thrown at: " + TimeUtils.formatAsDateTime(time) + "): "
                    + message;
        }
    }
}
