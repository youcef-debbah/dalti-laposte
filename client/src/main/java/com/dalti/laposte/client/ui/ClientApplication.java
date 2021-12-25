/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package com.dalti.laposte.client.ui;

import com.dalti.laposte.client.R;
import com.dalti.laposte.client.model.ClientActionReceiver;
import com.dalti.laposte.core.model.BasicActionReceiver;
import com.dalti.laposte.core.ui.AbstractQueueActivity;
import com.dalti.laposte.core.ui.AbstractQueueApplication;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public final class ClientApplication extends AbstractQueueApplication {

    public static final String FIREBASE_APP_NAME = "Dalti-laposte";
    private volatile Long initDuration;

    @Override
    protected void initFirebaseApp() {
        FirebaseOptions options = new FirebaseOptions.Builder()
                .setProjectId("dalti-laposte")
                .setStorageBucket("dalti-laposte.appspot.com")
                .setApiKey("AIzaSyDXQCVYJtfnhIPXwfRkHzrlOaYtdqqgT8g") // Required for Auth.
                .setApplicationId("1:208112057686:android:d613a73cd6ddbeaad42af7") // Required for Analytics.
                .build();
        FirebaseApp.initializeApp(this /* Context */, options, FIREBASE_APP_NAME);
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
        return ClientDashboardActivity.class;
    }

    @Override
    public Class<? extends BasicActionReceiver> getMainActionReceiver() {
        return ClientActionReceiver.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getActivationInfoActivity() {
        return ClientActivationInfoActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getActivationActivity() {
        return ActivationCodeFormActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getAboutUsActivity() {
        return ClientAboutUsActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getHelpActivity() {
        return ClientHelpActivity.class;
    }

    @Override
    public Class<? extends AbstractQueueActivity> getPrivacyPolicyActivity() {
        return ClientPrivacyPolicyActivity.class;
    }

    @Override
    public Integer getActivationActivityTitle() {
        return R.string.activate_application;
    }

    @Override
    public Integer getActivationActivityIcon() {
        return R.drawable.ic_baseline_qr_code_scanner_24;
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
