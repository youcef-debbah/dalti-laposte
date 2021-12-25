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

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.cleaningtools.ConcurrentCleanable;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.LDT;

/**
 * A thread-safe registry that holds references to collection of
 * {@linkplain ConcurrentObserver concurrent observers}.
 *
 * @param <T> The type of {@linkplain ConcurrentObserver observers} data parameter
 * @see MainObserversRegistry for non thread-safe equivalent
 */
@AnyThread
public final class ConcurrentObserversRegistry<T> implements ConcurrentCleanable {

    private static final String TAG = ConcurrentObserversRegistry.class.getSimpleName();

    public enum State {
        CLOSED, IDLE, PUBLISHING
    }

    private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
    private final Collection<WeakReference<ConcurrentObserver<T>>> observers = new CopyOnWriteArrayList<>();

    /**
     * Adds the given <var>observer</var> to the observers collection,
     *
     * @param observer the new observer
     * @throws IllegalStateException if this instance is closed
     */
    public void add(@NonNull final ConcurrentObserver<T> observer) {
        add(observer, null);
    }

    /**
     * Adds the given <var>observer</var> to the observers collection,
     * any <var>initialData</var> elements are published to the given
     * <var>observer</var> <em>before</em> actually adding the observer
     * to this registry (the order of <var>initialData</var> is preserved).
     *
     * @param observer    the new observer
     * @param initialData the initial data event to be published before adding the new observer
     * @throws IllegalArgumentException if the new observer is closed
     * @throws IllegalStateException    if this instance is closed or it is currently publishing
     *                                  (and initialData is not null or empty) which may happen when
     *                                  this method is called from an observer registered in this
     *                                  instance.
     */
    private void add(@NonNull final ConcurrentObserver<T> observer,
                     @Nullable final Iterable<? extends T> initialData) {
        Check.argNot(observer.isClosed());
        Check.stateNot(isClosed());

        if (initialData != null)
            if (state.compareAndSet(State.IDLE, State.PUBLISHING)) {
                try {
                    for (final T data : initialData)
                        if (!observer.isClosed()) {
                            Log.d(TAG, "publishing " + data + " to " + LDT.shortTypeName(observer));
                            observer.onChanged(data);
                        }
                } finally {
                    state.compareAndSet(State.PUBLISHING, State.IDLE);
                }
            } else
                throw new IllegalStateException("cannot publish because the registry is not IDLE");

        if (!isClosed() && !observer.isClosed())
            observers.add(new WeakReference<>(observer));
    }

    /**
     * Removes the given <var>observer</var> from the list of observers
     * that will get published events.
     * <p>
     * Note that removing an observer while other thread is publishing a new data
     * will <em>not prevent</em> it from getting notified by the new data.
     * </p>
     * <p>
     * Closing an observer before may do so (if it is still not notified yet),
     * however closing an observer after adding it to this registry (or event
     * merely keeping a reference to it) is strongly discouraged, note that
     * closing this instance will implicitly close all underlying observers
     * </p>
     *
     * @param observer the new observer
     */
    public void remove(@NonNull final ConcurrentObserver<T> observer) {
        for (WeakReference<ConcurrentObserver<T>> reference : observers) {
            final ConcurrentObserver<T> currentObserver = reference.get();
            if (currentObserver == null || currentObserver == observer || currentObserver.isClosed())
                observers.remove(reference);
        }
    }

    /**
     * Returns the count of observers in this collection. If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     * <p>
     * Note that the returned value reflect the state of this registry at a
     * time during the execution of this method however the state of this
     * registry state may have be changed by the time this method returns
     * to it's caller
     * </p>
     *
     * @return the observers count
     */
    public int count() {
        return observers.size();
    }

    /**
     * Publishes the given <var>data</var> to all observers.
     * <p>
     * Closed observers are removed during this method called <em>before</em> publishing
     * the new <var>data</var>
     * </p>
     * <p>
     * New observers that are added while publishing will <em>not</em> be included
     * in the current publishing
     * </p>
     *
     * @param data the data to be published
     * @throws IllegalStateException if this instance is closed or currently publishing
     *                               (which may happen when this method is called from an observer
     *                               registered in this instance)
     */
    public void publish(@Nullable final T data) {
        if (state.compareAndSet(State.IDLE, State.PUBLISHING)) {
            try {
                for (WeakReference<ConcurrentObserver<T>> reference : this.observers) {
                    final ConcurrentObserver<T> observer = reference.get();
                    if (observer == null || observer.isClosed())
                        this.observers.remove(reference);
                    else {
                        Log.d(TAG, "publishing " + data + " to " + LDT.shortTypeName(observer));
                        observer.onChanged(data);
                    }
                }
            } finally {
                state.compareAndSet(State.PUBLISHING, State.IDLE);
            }
        } else
            throw new IllegalStateException("cannot publish because the registry is not IDLE");
    }

    /**
     * Returns <tt>true</tt> if this collection contains at least one observer.
     * <p>
     * Note that the returned value reflect the state of this registry at a
     * time during the execution of this method however the state of this
     * registry state may have be changed by the time this method returns
     * to it's caller
     * </p>
     *
     * @return <tt>true</tt> if this collection contains at least one observer
     */
    public boolean notEmpty() {
        return !observers.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClosed() {
        return state.get() == State.CLOSED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        state.set(State.CLOSED);

        for (final WeakReference<ConcurrentObserver<T>> reference : observers) {
            final ConcurrentObserver<T> observer = reference.get();
            if (observer != null && !observer.isClosed())
                observer.close();
        }

        observers.clear();
    }
}
