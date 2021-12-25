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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.ForOverride;

/**
 * A ConcurrentObserver that has access to a single external context.
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
@AnyThread
public abstract class UnConcurrentObserver<C, D> implements ConcurrentObserver<D> {

    private final AtomicReference<C> context = new AtomicReference<>();

    /**
     * Constructs a new UnConcurrentObserver instance with the given <var>context</var>
     *
     * @param context the context to be accessed from {@link #onUpdate(Object, Object)}
     *                and {@link #onClose(Object)}
     */
    protected UnConcurrentObserver(@NotNull final C context) {
        this.context.set(Assert.nonNull(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onChanged(@Nullable final D data) {
        final C context = this.context.get();
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
    public final void close() {
        final C context = this.context.getAndSet(null);
        if (context != null)
            onClose(context);
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
    public final boolean isClosed() {
        return context.get() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public String toString() {
        final C context = this.context.get();
        if (context != null)
            return "ConcurrentObserver{" +
                    "isClosed=false" +
                    "context=" + context +
                    '}';
        else
            return "ConcurrentObserver{isClosed=true}";
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
