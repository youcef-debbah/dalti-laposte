package com.dalti.laposte.core.repositories;

import com.dalti.laposte.core.ui.Named;

public interface StringPreference extends Named {

    String name();

    String getDefaultString();

    int hashCode();

    boolean equals(Object obj);
}
