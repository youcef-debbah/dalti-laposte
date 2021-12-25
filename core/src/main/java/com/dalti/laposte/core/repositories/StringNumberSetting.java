package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.ui.NoteState;

public enum StringNumberSetting implements StringPreference, LongPreference {
    // settings
    MAX_TOKEN(9999),
    MAX_WAITING(9999),
    SCANNING_CAMERA_INDEX(-1),
    NOTE_STATE(NoteState.DEFAULT_ICON_STATE),

    OOREDOO_SMS_CONFIG(SimStrategy.DEFAULT_STRATEGY),
    MOBILIS_SMS_CONFIG(SimStrategy.DEFAULT_STRATEGY),
    DJEZZY_SMS_CONFIG(SimStrategy.DEFAULT_STRATEGY),
    OTHER_SMS_CONFIG(SimStrategy.DEFAULT_STRATEGY),
    ;

    private final long defaultLong;
    private final String defaultString;

    StringNumberSetting(long defaultLong) {
        this.defaultLong = defaultLong;
        this.defaultString = String.valueOf(defaultLong);
    }

    @Override
    public long getDefaultLong() {
        return defaultLong;
    }

    @Override
    public int getDefaultInteger() {
        return (int) defaultLong;
    }

    @Override
    public String getDefaultString() {
        return defaultString;
    }
}
