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

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import dz.jsoftware95.silverbox.android.observers.MainObserver;

public abstract class StatefulModel extends AndroidViewModel implements MutableStateOwner, Injectable {

    protected final String TAG = getClass().getSimpleName();

    protected StatefulModel(@NonNull final Application application) {
        super(application);
    }

    /**
     * {@linkplain #markAsRefreshing() Starts} refreshing and then check whether the current
     * data is up to date, if so it will get updated during this refresh operation
     */
    public abstract void startRefreshing();

    /**
     * sets the state of this model to "is refreshing" and notify observers.
     */
    public abstract void markAsRefreshing();

    /**
     * Sets the state of this model to "not refreshing" and notify observers.
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    public abstract void markAsRefreshDone();

    /**
     * Returns whether this model is currently refreshing it's data
     *
     * @return {@code true} if this model is still refreshing
     */
    public abstract boolean isRefreshing();

    /**
     * Calls {@link #startRefreshing()} or {@link #markAsRefreshDone()} depending on the argument
     * <code>refreshing</code>
     */
    public abstract void markAs(boolean refreshing);

    /**
     * Adds a new observer to this model. the observer must be active at the time of calling this method but it can
     * be closed later, a closed observer will get removed instead of calling it's callback.
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     *
     * @param observer the observer instance that will get event
     */
    public abstract void addModelObserver(@NonNull MainObserver<FrontendEvent> observer);

    /**
     * Removes the given observer from this model.
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     *
     * @param observer the observer to be removed from this model
     */
    public abstract void removeModelObserver(@NonNull MainObserver<FrontendEvent> observer);

    /**
     * Represents various events that can be triggered by an instance of a view model
     */
    public enum ModelEvent implements FrontendEvent {

        /**
         * Denotes that the model instance is about to get used for the first time
         */
        ACTIVATED,

        /**
         * Denotes that a refreshing is requested (either by the UI or the model itself)
         */
        REFRESHING,

        /**
         * Denotes that the refreshing was successful and new data has been fetched
         * but the model is not updated yet
         */
        REFRESH_DONE,

        /**
         * Denotes that the model has updated it's {@code exportable state}
         */
        STATE_IMPORTED,

        /**
         * Denotes that the model is about to save it's {@code state}; any data that will be
         * added to this model's {@code state} (by observers of this event) will be saved too
         */
        SAVING_STATE,

        /**
         * Denotes that the model is about to export it's {@code exportable state}; any data that will be
         * added to this model's {@code state} (by observers of this event) will be exported too
         */
        EXPORTING_STATE,
    }
}
