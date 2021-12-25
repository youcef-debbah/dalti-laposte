package com.dalti.laposte.core.repositories;

public enum SimpleProperty implements Property {
    CURRENT_ACTIVATED_CODE(Property.ID_108),
    LAST_ACTIVATIONS_UPDATE_TIME(Property.ID_109),
    LAST_ADMIN_ALARM_UPDATE_TIME(Property.ID_110),
    LAST_SERVICES_UPDATE_TIME(Property.ID_111),
    ;

    private final long key;

    SimpleProperty(long key) {
        this.key = key;
    }

    @Override
    public long key() {
        return key;
    }
}
