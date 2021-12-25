package com.dalti.laposte.core.repositories;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Set;

public interface SetPreference {

    String name();

    @NonNull
    default Set<String> getDefaultSet() {
        return Collections.emptySet();
    }

    int hashCode();

    boolean equals(Object obj);
}
