package com.dalti.laposte.core.repositories;

import dz.jsoftware95.silverbox.android.common.Check;

public enum IntegerSetting implements IntegerPreference {
    QR_SCANNER_VIBRATION_DURATION(100, 10, 1000, 10),
    UPDATE_SENT_VIBRATION_DURATION(50, 10, 500, 10),
    ;

    private final int defaultValue;
    private final int minValue;
    private final int maxValue;
    private final int step;

    IntegerSetting(int defaultValue) {
        this(defaultValue, 0, Integer.MAX_VALUE);
    }

    IntegerSetting(int defaultValue, int minValue, int maxValue) {
        this(defaultValue, minValue, maxValue, 1);
    }

    IntegerSetting(int defaultValue, int minValue, int maxValue, int step) {
        Check.that(minValue <= defaultValue);
        Check.that(defaultValue <= maxValue);
        Check.positiveInt(step);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;

    }

    @Override
    public int getDefaultInteger() {
        return defaultValue;
    }

    @Override
    public int getMinValue() {
        return minValue;
    }

    @Override
    public int getMaxValue() {
        return maxValue;
    }

    @Override
    public int getStep() {
        return step;
    }

    @Override
    public int getMaxSteps() {
        return maxValue / step;
    }

    @Override
    public int getMinSteps() {
        return minValue / step;
    }

    @Override
    public int getDefaultSteps() {
        return defaultValue / step;
    }
}
