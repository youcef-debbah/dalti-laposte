package com.dalti.laposte.client.repository;

import com.dalti.laposte.client.BuildConfig;
import com.dalti.laposte.core.util.BuildConfiguration;

import javax.inject.Inject;

public class ClientBuildConfiguration implements BuildConfiguration {
    private final int serverTarget;

    @Inject
    public ClientBuildConfiguration() {
        this.serverTarget = 1;
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
    public boolean isClient() {
        return true;
    }
}
