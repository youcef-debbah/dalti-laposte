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

package dz.jsoftware95.silverbox.android.observers;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import dz.jsoftware95.cleaningtools.ConcurrentCleanable;

/**
 * A thread-safe closable callback that can receive from {@link LiveData}.
 *
 * @param <T> The type of {@link #onChanged(Object)} parameter
 */
@AnyThread
public interface ConcurrentObserver<T> extends Observer<T>, ConcurrentCleanable {

    /**
     * Called when the data is changed.
     *
     * @param data the new data
     */
    @Override
    void onChanged(@Nullable final T data);
}
