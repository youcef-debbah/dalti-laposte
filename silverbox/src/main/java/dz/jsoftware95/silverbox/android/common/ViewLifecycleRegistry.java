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

package dz.jsoftware95.silverbox.android.common;

import android.util.Log;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A {@link LifecycleOwner} that wraps another {@code LifecycleOwner}
 * and project all events expect ON_DESTROY event which require {@link #markViewAsDestroyed()}
 * to be called to publish.
 * <p>
 * Note that {@link #markViewAsDestroyed()} should be called once when this instance
 * is in {@link State#CREATED} to move it to {@link State#DESTROYED}
 * </p>
 */
@MainThread
public final class ViewLifecycleRegistry implements LifecycleOwner {

    private final LifecycleRegistry registry = new LifecycleRegistry(this);

    /**
     * Constructs a new ViewLifecycleRegistry instance that wraps the state of the given
     * <var>source</var>
     *
     * @param source the wrapped {@code LifecycleOwner}
     */
    public ViewLifecycleRegistry(@NonNull final LifecycleOwner source) {
        Assert.isMainThread();
        source.getLifecycle().addObserver(new ViewLifecycleObserver(registry));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    /**
     * Marks this instance as {@link State#DESTROYED}.
     */
    public void markViewAsDestroyed() {
        registry.setCurrentState(State.DESTROYED);
    }

    /**
     * Throws a CloneNotSupportedException, always
     *
     * @return nothing, as this method would never finish normally
     * @throws CloneNotSupportedException always
     */
    @Override
    @NotNull
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Throws a NotSerializableException, always.
     *
     * @param outputStream serialization output stream (not used)
     * @throws NotSerializableException always
     */
    protected final void writeObject(final ObjectOutputStream outputStream) throws NotSerializableException {
        throw new NotSerializableException();
    }

    /**
     * Throws a NotSerializableException, always.
     *
     * @param inputStream serialization input stream (not used)
     * @throws NotSerializableException always
     */
    protected final void readObject(final ObjectInputStream inputStream) throws NotSerializableException {
        throw new NotSerializableException();
    }

    private static final class ViewLifecycleObserver implements DefaultLifecycleObserver {

        private static final String TAG = "ViewLifecycleObserver";

        private final LifecycleRegistry registry;

        ViewLifecycleObserver(@NonNull final LifecycleRegistry registry) {
            this.registry = Check.nonNull(registry);
        }

        private void setState(State state) {
            try {
                registry.setCurrentState(state);
            } catch (IllegalStateException e) {
                Log.w(TAG, "illegal state transaction to: " + state, e);
            }
        }

        @Override
        public void onCreate(@NonNull final LifecycleOwner owner) {
            setState(State.CREATED);
        }

        @Override
        public void onStart(@NonNull final LifecycleOwner owner) {
            setState(State.STARTED);
        }

        @Override
        public void onResume(@NonNull final LifecycleOwner owner) {
            setState(State.RESUMED);
        }

        @Override
        public void onPause(@NonNull final LifecycleOwner owner) {
            setState(State.STARTED);
        }

        @Override
        public void onStop(@NonNull final LifecycleOwner owner) {
            setState(State.CREATED);
        }

        @Override
        public void onDestroy(@NonNull final LifecycleOwner owner) {
            setState(State.DESTROYED);
        }
    }
}
