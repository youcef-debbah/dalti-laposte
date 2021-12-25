package com.dalti.laposte.core.repositories;

import androidx.annotation.NonNull;

import dz.jsoftware95.silverbox.android.backend.Item;
import dz.jsoftware95.silverbox.android.common.StringUtil;

public class AlarmPhonePreference implements StringPreference {

    public static final String ALARM_PHONE_PREFIX = "ALARM_PHONE_";
    private final String name;
    private final long alarmID;

    public AlarmPhonePreference(long alarmID) {
        if (alarmID <= Item.AUTO_ID)
            throw new IllegalArgumentException("illegal alarm id: " + alarmID);
        this.name = ALARM_PHONE_PREFIX + alarmID;
        this.alarmID = alarmID;
    }

    public long getAlarmID() {
        return alarmID;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String getDefaultString() {
        return "";
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmPhonePreference that = (AlarmPhonePreference) o;
        return alarmID == that.alarmID;
    }

    @Override
    public int hashCode() {
        return StringUtil.hash(alarmID);
    }
}
