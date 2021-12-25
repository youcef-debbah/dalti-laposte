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

package dz.jsoftware95.silverbox.android.middleware;

import android.app.Application;

import androidx.annotation.MainThread;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.common.Assert;

@MainThread
public abstract class StatefulApplication extends Application {

    private static final String APPLICATION_NOT_INJECTED = "Application not injected: ";

    private volatile boolean injected = false;

    @Inject
    public void markAsInjected() {
        Assert.not(isInjected());
        injected = true;
    }

    public final boolean isInjected() {
        return injected;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Assert.that(isInjected(), APPLICATION_NOT_INJECTED + toString());
    }
}