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

import org.jetbrains.annotations.Contract;

/**
 * An event that is originally published from backend layer of the application
 * (Database , Network IO, Local IO, etc).
 */
@AnyThread
public interface BackendEvent {

    /**
     * Returns whether the code that is handling this event should assume that it's
     * state is outdated.
     *
     * @return whether the handlers of this event are expected to invalidate any cashed state
     * that is related to the this event
     */
    @Contract(pure = true)
    default boolean shouldRestState() {
        return false;
    }

    @Contract(pure = true)
    default boolean shouldStartRefreshing() {
        return false;
    }

    @Contract(pure = true)
    default boolean shouldStopRefreshing() {
        return false;
    }
}
