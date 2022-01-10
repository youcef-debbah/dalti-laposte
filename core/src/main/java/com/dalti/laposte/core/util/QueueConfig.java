package com.dalti.laposte.core.util;

import java.util.concurrent.TimeUnit;

import dz.jsoftware95.common.ProjectPhase;

public final class QueueConfig {
    public static final int ANR_TIMEOUT_SECONDS = 5;

    public static final int ON_DURATION = 1250;
    public static final int OFF_DURATION = 1500;
    public static final long[] ALARM_VIBRATION_PATTERN = {0, ON_DURATION, OFF_DURATION, ON_DURATION};
    public static final long[] INFO_VIBRATION_PATTERN = {0, 350, 250, 350};

    public static final long INC_TOKEN_DELAY = TimeUnit.SECONDS.toMillis(5);
}
