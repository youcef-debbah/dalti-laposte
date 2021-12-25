package com.dalti.laposte.core.ui;

public enum QueueNotifications {
    COMPACT_DASHBOARD_ACTIVATED,
    QUEUE_STATE,
    ;

    public int id() {
        return ordinal() + 1;
    }
}
