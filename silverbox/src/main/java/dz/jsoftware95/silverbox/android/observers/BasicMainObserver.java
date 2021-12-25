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

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.AutoCleaner;
import dz.jsoftware95.silverbox.android.common.ForOverride;

/**
 * A simple implementation of MainObserver with boolean flag to track
 * whether the instance is closed or no.
 *
 * @param <D> The type of {@link #onChanged(Object)} parameter
 */
@MainThread
public abstract class BasicMainObserver<D> implements MainObserver<D> {

    private boolean isClosed = false;

    /**
     * Constructs a new BasicMainObserver instance.
     */
    protected BasicMainObserver() {
        Assert.isMainThread();
    }

    /**
     * Constructs a new BasicMainObserver that will get closed
     * automatically when the given <var>Lifecycle</var> fire
     * {@link Lifecycle.Event#ON_DESTROY} event
     *
     * @param lifecycle the lifecycle of this observer
     */
    protected BasicMainObserver(@NonNull final Lifecycle lifecycle) {
        Assert.isMainThread();
        lifecycle.addObserver(new AutoCleaner(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onChanged(@Nullable final D data) {
        Assert.isMainThread();
        if (!isClosed)
            onUpdate(data);
    }

    /**
     * Called when the data is changed <em>and</em> this instance is not closed.
     *
     * @param data the new data
     */
    @ForOverride
    protected void onUpdate(@Nullable final D data) {}

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isClosed() {
        Assert.isMainThread();
        return isClosed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        Assert.isMainThread();
        if (!isClosed)
            try {
                preClose();
            } finally {
                isClosed = true;
            }
    }

    /**
     * A hook that gets called before closing this instance.
     */
    @ForOverride
    protected void preClose() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public String toString() {
        return "MainObserver{" +
                "isClosed=" + isClosed +
                '}';
    }

    /**
     * Throws a CloneNotSupportedException, always
     *
     * @return nothing, as this method would never finish normally
     * @throws CloneNotSupportedException always
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Throws a NotSerializableException, always.
     * <p>
     * Observers cannot be Serialized because they may
     * contains references to non-serializable contexts
     * </p>
     *
     * @param outputStream serialization output stream (not used)
     * @throws NotSerializableException always
     */
    protected final void writeObject(final ObjectOutputStream outputStream) throws NotSerializableException {
        throw new NotSerializableException("Observers cannot be Serialized because it " +
                "may contains references to non-serializable contexts");
    }

    /**
     * Throws a NotSerializableException, always.
     * <p>
     * Observers cannot be Serialized because they may
     * contains references to non-serializable contexts
     * </p>
     *
     * @param inputStream serialization input stream (not used)
     * @throws NotSerializableException always
     */
    protected final void readObject(final ObjectInputStream inputStream) throws NotSerializableException {
        throw new NotSerializableException("Observers cannot be Serialized because they " +
                "may contains references to non-serializable contexts");
    }
}
