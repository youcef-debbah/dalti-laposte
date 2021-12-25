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
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

import dz.jsoftware95.cleaningtools.AutoCleanable;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.LDT;
import dz.jsoftware95.silverbox.android.concurrent.BasicJob;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;

/**
 * A registry that holds references to collection of {@linkplain MainObserver main observers}.
 *
 * @param <T> The type of {@linkplain MainObserver observers} data parameter
 * @see ConcurrentObserversRegistry for a thread-safe equivalent
 */
@MainThread
public final class MainObserversRegistry<T> implements AutoCleanable {

    private static final String TAG = MainObserversRegistry.class.getSimpleName();

    private enum State {
        CLOSED, IDLE, PUBLISHING
    }

    private volatile State state = State.IDLE;
    private final Collection<WeakReference<MainObserver<T>>> observers = new CopyOnWriteArrayList<>();

    private final BasicJob publishNullJob = new BasicJob(true) {
        @Override
        protected void doFromMain() {
            publish(null);
        }
    };

    /**
     * Constructs an empty MainObserversRegistry
     */
    @AnyThread
    public MainObserversRegistry() {
    }

    /**
     * Adds the given <var>observer</var> to the observers collection,
     *
     * @param observer the new observer
     * @throws IllegalStateException if this instance is closed
     */
    public final void add(@NonNull final MainObserver<T> observer) {
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
    public final void add(@NonNull final MainObserver<T> observer,
                          @Nullable final Iterable<? extends T> initialData) {
        Assert.isMainThread();
        Check.argNot(observer.isClosed());

        final State state = this.state;
        Check.state(state != State.CLOSED);

        if (initialData != null)
            if (state == State.IDLE)
                try {
                    this.state = State.PUBLISHING;
                    for (final T data : initialData)
                        if (!observer.isClosed()) {
                            Log.d(TAG, "publishing initial data " + data + " to " + LDT.shortTypeName(observer));
                            observer.onChanged(data);
                        }
                } finally {
                    if (this.state == State.PUBLISHING)
                        this.state = State.IDLE;
                }
            else
                throw new IllegalStateException("cannot publish when the registry is " + state);

        if (!observer.isClosed())
            observers.add(new WeakReference<>(observer));
    }

    /**
     * Removes the given <var>observer</var> from the list of observers
     * that will get published events.
     *
     * @param observer the new observer
     */
    public void remove(@NonNull final MainObserver<T> observer) {
        Assert.isMainThread();
        for (WeakReference<MainObserver<T>> reference : observers) {
            final MainObserver<T> currentObserver = reference.get();
            if (currentObserver == null || currentObserver == observer || currentObserver.isClosed())
                observers.remove(reference);
        }
    }

    /**
     * Remove any closed <var>observer</var> from the list of observers.
     */
    public void clean() {
        Assert.isMainThread();
        for (WeakReference<MainObserver<T>> observer : observers) {
            final MainObserver<T> currentObserver = observer.get();
            if (currentObserver == null || currentObserver.isClosed())
                observers.remove(observer);
        }
    }

    /**
     * Returns the count of observers in this collection. If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
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
     *
     * @param data the data to be published
     * @throws IllegalStateException if this instance is closed or currently publishing
     *                               (which may happen when this method is called from an observer
     *                               registered in this instance)
     */
    public void publish(@Nullable final T data) {
        Assert.isMainThread();
        final State state = this.state;
        if (state == State.IDLE)
            try {
                this.state = State.PUBLISHING;
                for (WeakReference<MainObserver<T>> reference : this.observers) {
                    final MainObserver<T> observer = reference.get();
                    if (observer == null || observer.isClosed())
                        this.observers.remove(reference);
                    else {
                        Log.d(TAG, "publishing " + data + " to " + LDT.shortTypeName(observer));
                        observer.onChanged(data);
                    }
                }
            } finally {
                if (this.state == State.PUBLISHING)
                    this.state = State.IDLE;
            }
        else
            throw new IllegalStateException("cannot publish when the registry is " + state);
    }

    @AnyThread
    public void postPublish(@Nullable final T data) {
        if (data == null)
            publishNullJob.execute();
        else
            new UnJob<T>(data) {
                @Override
                protected void doFromMain(@NonNull T context) {
                    publish(context);
                }
            }.execute();
    }

    /**
     * Returns <tt>true</tt> if this collection contains at least one observer.
     *
     * @return <tt>true</tt> if this collection contains at least one observer
     */
    public boolean notEmpty() {
        Assert.isMainThread();
        return !observers.isEmpty();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    @AnyThread
    public boolean isClosed() {
        return state == State.CLOSED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        Assert.isMainThread();
        state = State.CLOSED;

        for (final WeakReference<MainObserver<T>> reference : observers) {
            final MainObserver<T> observer = reference.get();
            if (observer != null && !observer.isClosed())
                observer.close();
        }

        observers.clear();
    }
}
