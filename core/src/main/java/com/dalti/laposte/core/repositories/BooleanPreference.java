package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.ui.Named;

public interface BooleanPreference extends Named {

    String name();

    boolean getDefaultBoolean();
}
