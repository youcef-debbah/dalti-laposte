/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import dz.jsoftware95.silverbox.android.observers.MainObserver;

@AnyThread
public interface Repository {

    @MainThread
    void invalidate();

    @MainThread
    void addObserver(@NonNull MainObserver<BackendEvent> observer);

    @MainThread
    void removeObserver(@NonNull MainObserver<BackendEvent> observer);

    boolean isRefreshing();

    boolean isRefreshedBefore();
}
