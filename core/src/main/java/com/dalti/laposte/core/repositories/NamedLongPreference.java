package com.dalti.laposte.core.repositories;

public class NamedLongPreference implements LongPreference {

    public static final String PREFIX_LAST_FETCH = "LAST_FETCH_";

    private final String name;
    private final long defaultLong;
    private final int defaultInteger;

    public NamedLongPreference(String name) {
        this(name, 0);
    }

    public NamedLongPreference(String name, long defaultValue) {
        this.name = name;
        this.defaultLong = defaultValue;
        this.defaultInteger = (int) defaultValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public long getDefaultLong() {
        return defaultLong;
    }

    @Override
    public int getDefaultInteger() {
        return defaultInteger;
    }
}
