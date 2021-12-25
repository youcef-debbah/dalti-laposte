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
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnJob;
import dz.jsoftware95.silverbox.android.observers.MainObserver;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

@MainThread
public final class LiveDataWrapper<T> {

    private final Integer delay;
    private final MediatorLiveData<T> mediatorLiveData;
    private final Job postNullSourceJob = new UnJob<LiveDataWrapper<T>>(this, true) {
        @Override
        protected void doFromMain(@NonNull final LiveDataWrapper<T> mediator) {
            mediator.setSource(null);
        }
    };

    public LiveDataWrapper() {
        this(null, null);
    }

    public LiveDataWrapper(T data) {
        this(new MutableLiveData<>(data), null);
    }

    public LiveDataWrapper(LiveData<T> source) {
        this(source, null);
    }

    public LiveDataWrapper(LiveData<T> source, Integer delayDuration) {
        delay = delayDuration;
        mediatorLiveData = new MediatorLiveData<>();
        if (source != null)
            setSource(source);
    }

    @Nullable
    private volatile State<T> currentState = null;

    @NonNull
    @Contract(pure = true)
    @AnyThread
    public final MutableLiveData<T> getLiveData() {
        return mediatorLiveData;
    }

    @CallSuper
    public void setSource(@Nullable final LiveData<T> newSource) {
        setSource(newSource, newSourceObserver(mediatorLiveData, delay));
    }

    public void setSource(@Nullable LiveData<T> newSource, Observer<T> updater) {
        Objects.requireNonNull(updater);
        clearSource();

        if (newSource != null) {
            mediatorLiveData.addSource(newSource, updater);
            this.currentState = new State<>(newSource, updater);
        }
    }

    public void clearSource() {
        Check.isMainThread();
        final State<T> currentState = this.currentState;
        if (currentState != null) {
            mediatorLiveData.removeSource(currentState.source);
            if (currentState.observer instanceof AutoCloseable) {
                try {
                    ((AutoCloseable)currentState.observer).close();
                } catch (Exception e) {
                    throw new RuntimeException("could not close", e);
                }
            }
            this.currentState = null;
        }
    }

    @AnyThread
    public void postSource(@Nullable final LiveData<T> newSource) {
        if (newSource == null)
            postNullSourceJob.execute();
        else
            newPostSourceJob(this, newSource).execute();
    }

    @NonNull
    @Override
    public String toString() {
        final State<T> state = this.currentState;
        if (state != null)
            return "LiveDataWrapper(" +
                    ", source: " + state.source +
                    ", observer: " + state.observer +
                    ')';
        else
            return "LiveDataWrapper(empty)";
    }

    private static <U> Job newPostSourceJob(final LiveDataWrapper<U> mediator,
                                            final LiveData<U> newSource) {
        return new DuoJob<LiveDataWrapper<U>, LiveData<U>>(mediator, newSource) {
            @Override
            protected void doFromMain(@NonNull final LiveDataWrapper<U> wrapper, @NonNull final LiveData<U> newSource) {
                wrapper.setSource(newSource);
            }
        };
    }

    private static <U> MainObserver<U> newSourceObserver(@NonNull final MutableLiveData<U> liveData,
                                                         @Nullable final Integer delay) {
        if (delay == null)
            return new UnMainObserver<MutableLiveData<U>, U>(liveData) {
                @Override
                @MainThread
                protected void onUpdate(@NonNull final MutableLiveData<U> liveData,
                                        @Nullable final U data) {
                    liveData.setValue(data);
                }
            };
        else
            return new DelayedSourceObserver<>(liveData::postValue, delay);
    }

    @AnyThread
    public void postValue(T value) {
        mediatorLiveData.postValue(value);
    }

    public void setValue(T value) {
        mediatorLiveData.setValue(value);
    }

    private static final class State<T> {
        @NonNull
        final LiveData<T> source;
        @NonNull
        final Observer<T> observer;

        State(@NonNull final LiveData<T> source,
              @NonNull final Observer<T> observer) {
            this.source = Assert.nonNull(source);
            this.observer = Assert.nonNull(observer);
        }
    }

}