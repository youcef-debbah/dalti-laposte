package com.dalti.laposte.core.util;

import com.dalti.laposte.R;

import dz.jsoftware95.queue.common.GlobalConf;

public interface BuildConfiguration {

    String DEFAULT_PRODUCTION_HOST_NAME = "www.dalti-laposte.com";

    String DEFAULT_QUEUE_API_URL = GlobalConf.QUEUE_API_V_1;
    String DEFAULT_EMULATOR_LOCAL_HOST_NAME = "10.0.2.2";
    String DEFAULT_LOCAL_URL_SCHEMA = "http://";
    String DEFAULT_PRODUCTION_URL_SCHEMA = "https://";

    String DEFAULT_SERVICES_API_URL = DEFAULT_PRODUCTION_URL_SCHEMA + DEFAULT_PRODUCTION_HOST_NAME + DEFAULT_QUEUE_API_URL;

    static String getLocalServicesApiUrl() {
        return DEFAULT_LOCAL_URL_SCHEMA + QueueUtils.getString(R.string.hostname) + DEFAULT_QUEUE_API_URL;
    }

    static String getEmulatorLocalServicesApiUrl() {
        return DEFAULT_LOCAL_URL_SCHEMA + DEFAULT_EMULATOR_LOCAL_HOST_NAME + DEFAULT_QUEUE_API_URL;
    }

    int getVersionCode();

    String getVersionName();

    default String getFullVersionName() {
        if (QueueUtils.isTesting())
            return getVersionName() + "-test";
        else
            return getVersionName();
    }

    int getServerTarget();

    default boolean isAdmin() {
        return false;
    }

    default boolean isClient() {
        return false;
    }

    default int getSignedVersionCode() {
        return (QueueUtils.isTesting() ? -1 : 1) * getVersionCode();
    }
}
