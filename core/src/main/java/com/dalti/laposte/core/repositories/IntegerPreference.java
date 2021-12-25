package com.dalti.laposte.core.repositories;

public interface IntegerPreference {

    String name();

    int getDefaultInteger();

    int getMinValue();

    int getMaxValue();

    int getStep();

    int getMaxSteps();

    int getMinSteps();

    int getDefaultSteps();
}
