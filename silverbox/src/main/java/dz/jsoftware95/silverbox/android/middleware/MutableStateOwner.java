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

package dz.jsoftware95.silverbox.android.middleware;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

public interface MutableStateOwner extends
        LifecycleOwner,
        StateOwner,
        ReadableStateOwner,
        WritableStateOwner,
        ExportableStateOwner {

    /**
     * Returns the Lifecycle of this instance.
     *
     * @return The lifecycle of this instance
     */
    @NonNull
    @Override
    Lifecycle getLifecycle();

    /**
     * Returns {@code true} if this instance has been already activated (used at least once).
     * <p>
     * Note you can get the same info by calling:
     * <pre>{@code
     *     boolean isStared = getLifecycle().getCurrentState().isAtLeast({@link Lifecycle.State#STARTED});
     * }</pre>
     *
     * @return whether this instance has been activated or no
     */
    boolean isActivated();

    /**
     * Saves the current state of this instance into the given {@code Bundle}
     *
     * @param state the output {@code Bundle} to save state into
     * @throws NullPointerException if {@code state} is {@code null}
     */
    void saveState(@NonNull final Bundle state);

    /**
     * Loads the given state to into this instance if it is not {@code null}, else ignores the call
     *
     * @param state the state to be loaded
     */
    void loadState(@Nullable final Bundle state);
}
