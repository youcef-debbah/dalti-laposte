package com.dalti.laposte.core.repositories;

import dz.jsoftware95.queue.common.GlobalConf;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public final class LaunchedTurnAlarm {

    private final long progressID;
    private final long alarmTargetTime;
    private final long launchTime;

    private LaunchedTurnAlarm(long progressID, long alarmTargetTime, long launchTime) {
        this.progressID = progressID;
        this.alarmTargetTime = alarmTargetTime;
        this.launchTime = launchTime;
    }

    public long getProgressID() {
        return progressID;
    }

    public long getAlarmTargetTime() {
        return alarmTargetTime;
    }

    public long getLaunchTime() {
        return launchTime;
    }

    @Override
    public String toString() {
        return progressID + GlobalConf.SEPARATOR + alarmTargetTime + GlobalConf.SEPARATOR + launchTime;
    }

    public static String encode(LaunchedTurnAlarm value) {
        return value == null ? null : value.toString();
    }

    public static LaunchedTurnAlarm decode(String value) {
        if (StringUtil.isBlank(value))
            return null;
        else {
            String[] tokens = value.split(GlobalConf.SEPARATOR, 4);
            Long progressID = StringUtil.parseLong(GlobalUtil.getElement(0, tokens));
            Long alarmTargetTime = StringUtil.parseLong(GlobalUtil.getElement(1, tokens));
            Long launchTime = StringUtil.parseLong(GlobalUtil.getElement(2, tokens));
            if (progressID != null && alarmTargetTime != null && launchTime != null)
                return new LaunchedTurnAlarm(progressID, alarmTargetTime, launchTime);
            else
                return null;
        }
    }

    public static LaunchedTurnAlarm from(long progressID, long alarmTargetTime) {
        return new LaunchedTurnAlarm(progressID, alarmTargetTime, System.currentTimeMillis());
    }

    public static String encodeString(long progressID, long alarmTargetTime) {
        return encode(from(progressID, alarmTargetTime));
    }
}
