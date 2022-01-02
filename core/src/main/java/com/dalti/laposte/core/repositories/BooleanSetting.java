package com.dalti.laposte.core.repositories;

public enum BooleanSetting implements BooleanPreference {

    RETRY_WITH_IMPLICIT_ACTIVATION(true),
    ENABLE_SMS_ALARM_CONFIRMATION(true),
    ENABLE_SMS_ALARM(true),
    ENABLE_APP_CHECK(true),

    // local settings

    IMMEDIATE_FEEDBACK(true),
    SHOW_COMPACT_UI_NOTIFICATION(true),
    VIBRATE_ON_UPDATE_SENT(false),

    INIT_TURN_ALARMS(true),
    WAIT_APPLICATION_ID(true),


    UNKNOWN_AVAILABILITY_NOTIFICATION_SHOWN(false),
    IDLE_ADMIN_NOTIFICATION_SHOWN(false),

    UNKNOWN_AVAILABILITY_NOTIFICATION(true),
    TOAST_FROM_BACKGROUND(false),
    SMS_REQUESTS_ENABLED(false),
    SCANNER_FLASH_LIGHT(false),

    ENABLE_FIREBASE_SMS_AUTH(false),

    FORCE_APP_CHECK_REFRESH(false),

    ;

    private final boolean defaultValue;

    BooleanSetting(boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public boolean getDefaultBoolean() {
        return defaultValue;
    }
}
