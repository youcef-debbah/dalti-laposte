package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.ui.Named;

public interface LongPreference extends Named {

    String name();

    long getDefaultLong();

    int getDefaultInteger();
}
