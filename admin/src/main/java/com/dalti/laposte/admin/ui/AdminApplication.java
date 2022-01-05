package com.dalti.laposte.admin.ui;

import com.dalti.laposte.admin.BuildConfig;
import com.dalti.laposte.admin.R;
import com.dalti.laposte.admin.model.AdminActionReceiver;
import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class AdminApplication extends AbstractQueueApplication {

    public static final String FIREBASE_APP_NAME = "Dalti-laposte-admin";
    private volatile Long initDuration;

    @Override
    protected FirebaseApp initFirebaseApp() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("dalti-laposte")
                .setStorageBucket("dalti-laposte.appspot.com")
                .setApiKey(BuildConfig.API_KEY) // Required for Auth.
                .setApplicationId("1:208112057686:android:befd6bacf7f8892ad42af7") // Required for Analytics.
                .build();
        return FirebaseApp.initializeApp(this /* Context */, options, FIREBASE_APP_NAME);
    }

    @Override
    public FirebaseApp getFirebaseApp() {
        return FirebaseApp.getInstance(FIREBASE_APP_NAME);
    }

    @Override
    public FirebaseAuth getFirebaseAuth() {
        FirebaseAuth auth = FirebaseAuth.getInstance(getFirebaseApp());
        auth.setLanguageCode("fr");
        return auth;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getMainActivity() {
        return AdminDashboardActivity.class;
    }

    @Override
    public Class<? extends BasicActionReceiver> getMainActionReceiver() {
        return AdminActionReceiver.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getActivationInfoActivity() {
        return AdminActivationInfoActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getActivationActivity() {
        return ProfileActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getAboutUsActivity() {
        return AdminAboutUsActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getHelpActivity() {
        return AdminHelpActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getPrivacyPolicyActivity() {
        return AdminPrivacyPolicyActivity.class;
    }

    @Override
    public Integer getActivationActivityTitle() {
        return R.string.profile;
    }

    @Override
    public Integer getActivationActivityIcon() {
        return R.drawable.ic_baseline_account_circle_24;
    }

    @Override
    public Long getInitDuration() {
        return initDuration;
    }

    @Override
    public void onCreate() {
        long t0 = System.nanoTime();
        super.onCreate();
        initDuration = System.nanoTime() - t0;
    }
}
