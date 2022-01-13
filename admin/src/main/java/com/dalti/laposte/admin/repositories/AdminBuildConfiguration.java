package com.dalti.laposte.admin.repositories;

import com.dalti.laposte.admin.BuildConfig;
import com.dalti.laposte.core.util.BuildConfiguration;

import javax.inject.Inject;

public class AdminBuildConfiguration implements BuildConfiguration {
    private final int serverTarget;

    @Inject
    public AdminBuildConfiguration() {
        this.serverTarget = 2;
    }

    @Override
    public int getVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public String getVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getServerTarget() {
        return serverTarget;
    }

    @Override
    public boolean isAdmin() {
        return true;
    }
}
