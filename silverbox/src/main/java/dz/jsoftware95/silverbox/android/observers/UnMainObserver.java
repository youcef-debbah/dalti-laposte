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
import androidx.lifecycle.LifecycleOwner;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.AutoCleaner;
import dz.jsoftware95.silverbox.android.common.ForOverride;

/**
 * A MainObserver that has access to a single external context.
 *
 * <p>
 * the context object will be available as the first parameter
 * of {@link #onUpdate(Object, Object)} and {@link #onClose(Object)}.
 * The context will get nulled when this instance {@linkplain #isClosed() is closed}
 * </p>
 *
 * @param <C> the type of the context
 * @param <D> The type of {@link #onChanged(Object)} parameter
 */
@MainThread
public abstract class UnMainObserver<C, D> implements MainObserver<D> {

    @Nullable
    private C context;

    /**
     * Constructs a new UnMainObserver instance with the given <var>context</var>
     *
     * @param context the context to be accessed from {@link #onUpdate(Object, Object)}
     *                and {@link #onClose(Object)}
     */
    protected UnMainObserver(@NonNull final C context) {
        this.context = Assert.nonNull(context);
        if (context instanceof LifecycleOwner) {
            final LifecycleOwner owner = (LifecycleOwner) context;
            owner.getLifecycle().addObserver(new AutoCleaner(this));
        }
    }

    /**
     * Constructs a new UnMainObserver instance with the given <var>context</var>
     * that will get closed automatically when the given <var>Lifecycle</var> fire
     * {@link Lifecycle.Event#ON_DESTROY} event
     *
     * @param lifecycleOwner the lifecycleOwner of this observer
     * @param context   the context to be accessed from {@link #onUpdate(Object, Object)}
     *                  and {@link #onClose(Object)}
     */
    protected UnMainObserver(@NonNull final LifecycleOwner lifecycleOwner,
                             @NonNull final C context) {
        this.context = Assert.nonNull(context);
        lifecycleOwner.getLifecycle().addObserver(new AutoCleaner(this));
    }

    /**
     * Constructs a new UnMainObserver instance with the given <var>context</var>
     * that will get closed automatically when the given <var>Lifecycle</var> fire
     * {@link Lifecycle.Event#ON_DESTROY} event
     *
     * @param lifecycle the lifecycle of this observer
     * @param context   the context to be accessed from {@link #onUpdate(Object, Object)}
     *                  and {@link #onClose(Object)}
     */
    protected UnMainObserver(@NonNull final Lifecycle lifecycle,
                             @NonNull final C context) {
        this.context = Assert.nonNull(context);
        lifecycle.addObserver(new AutoCleaner(this));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onChanged(@Nullable final D data) {
        Assert.isMainThread();
        final C context = this.context;
        if (context != null)
            onUpdate(context, data);
    }

    /**
     * Called when the data is changed <em>and</em> this instance is not closed.
     *
     * @param context the context that was given at construction time
     * @param data    the new data
     */
    @ForOverride
    protected void onUpdate(@NonNull final C context,
                            @Nullable final D data) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isClosed() {
        Assert.isMainThread();
        return context == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        Assert.isMainThread();
        final C context = this.context;
        if (context != null) {
            this.context = null;
            onClose(context);
        }
    }

    /**
     * A hook that will gets called when this instance is being closed
     *
     * @param context the context that was given at construction time
     */
    @ForOverride
    protected void onClose(@NonNull final C context) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public String toString() {
        final C context = this.context;
        if (context != null)
            return "MainObserver{" +
                    "isClosed=false" +
                    "context=" + context +
                    '}';
        else
            return "MainObserver{isClosed=true}";
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
