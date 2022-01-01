package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.entity.TurnAlarm;

import java.util.concurrent.TimeUnit;

public enum LongSetting implements LongPreference {
    SMS_TIMEOUT(35L),
    MAX_SERVICE_CACHE(TimeUnit.DAYS.toMillis(7)),
    MAX_ACTIVATION_CACHE(TimeUnit.HOURS.toMillis(12)),
    MAX_ADMIN_ALARM_CACHE(TimeUnit.MINUTES.toMillis(0)),
    MAX_PROGRESS_CACHE_IN_DAYS(1),
    LONG_TOAST_DURATION_THRESHOLD(30),

    CONNECT_TIMEOUT_IN_SECONDS(10),
    READ_TIMEOUT_IN_SECONDS(10),
    WRITE_TIMEOUT_IN_SECONDS(10),
    APP_CHECK_TIMEOUT_IN_MILLIS(10_000),

    ACTIVATION_HINTS_COUNT(2),
    ACTIVATION_HINTS_RESET(1),

    UPDATE_HINTS_COUNT(1),
    UPDATE_HINTS_RESET(1),

    SMS_REQUESTED(0),
    SMS_SENT(0),
    SMS_FAILED(0),
    SMS_DELIVERED(0),
    SMS_IGNORED(0),

    SMS_ALARM_DURATION_DEFAULT_INPUT(TurnAlarm.Settings.DEFAULT_BEFOREHAND_INPUT.getDefaultLong()),
    SMS_ALARM_MIN_LIQUIDITY_DEFAULT_INPUT(TurnAlarm.Settings.DEFAULT_MIN_LIQUIDITY_VALUE.getDefaultLong()),

    CLOSE_TIME_TIMEOUT_IN_DAYS(1),

    BANNER_DELAY(0),
    NETWORK_INDICATOR_DELAY(0),
    TTL_WITHOUT_USER_INTERACTION(TimeUnit.HOURS.toMillis(36)),

    STATE_LOG_COOLDOWN(TimeUnit.MINUTES.toMillis(4)),

    APP_ID_DELAY(TimeUnit.SECONDS.toMillis(30)),

    //    version code: 1955121 (packed with some android 5.0 devices)
//    MIN_GOOGLE_SERVICES_VERSION(7571430),
    LAST_AUTO_REFRESH(0),

    DEFAULT_BUFFER_SIZE(64 * 1024),

    WEB_PAGE_FETCH_COOLDOWN(TimeUnit.HOURS.toMillis(10)),

    CLEARED_ALARMS_COUNT(0),

    CLEARED_ALARMS_TO_SHOW_RATING(3),
    CLOSE_DELAY(TimeUnit.SECONDS.toMillis(5)),
    ;

    private final long defaultLong;

    LongSetting(long defaultLong) {
        this.defaultLong = defaultLong;
    }

    @Override
    public long getDefaultLong() {
        return defaultLong;
    }

    @Override
    public int getDefaultInteger() {
        return (int) defaultLong;
    }
}
