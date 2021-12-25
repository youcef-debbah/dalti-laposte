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

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.State;
import androidx.lifecycle.LifecycleRegistry;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.inject.Inject;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.ForOverride;
import dz.jsoftware95.silverbox.android.common.LDT;
import dz.jsoftware95.silverbox.android.observers.MainObserver;
import dz.jsoftware95.silverbox.android.observers.MainObserversRegistry;

/**
 * A base View Model implementation that support refreshing, state and lifecycle events.
 */
@MainThread
public abstract class BasicModel extends StatefulModel {

    public static final State ACTIVATED_STATE = State.STARTED;

    private static final String CANONICAL_NAME_MISSING = "The class of this model does not have a canonical name: ";

    private static final String MUST_INJECT_FIRST = "model must be injected before it gets activated";

    @SuppressLint("StaticFieldLeak")
    private final LifecycleRegistry lifecycle;

    private final MainObserversRegistry<FrontendEvent> observers = new MainObserversRegistry<>();

    private final Bundle state = new Bundle();

    private final Bundle exportableState = new Bundle();

    private boolean isInjected;

    private boolean isRefreshing;

    /**
     * Creates a new Refreshable View Model with an empty refresh observers list
     *
     * @param application the application instance to be used by this model
     */
    protected BasicModel(final Application application) {
        super(Assert.nonNull(application));
        lifecycle = new LifecycleRegistry(this);
    }

    /**
     * Marks this model as injected, <em>do NOT call this method directly</em>,
     * instead use a dependency injection framework
     * (recommended framework: <a href="https://google.github.io/dagger">Dagger 2</a>).
     */
    @Inject
    public void markAsInjected() {
        Assert.not(isInjected);
        isInjected = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public final boolean isInjected() {
        return isInjected;
    }

    /**
     * Activates this instance by calling {@link #preActivate()}, then Publishes
     * {@linkplain ModelEvent#ACTIVATED activated} event if this instance hasn't activated yet,
     * do nothing otherwise
     *
     * @throws IllegalStateException if this instance is not {@linkplain #isInjected() injected}
     */
    protected final void ensureActivated() {
        if (!isActivated()) {
            if (!isInjected)
                throw new IllegalStateException(MUST_INJECT_FIRST);

            lifecycle.setCurrentState(ACTIVATED_STATE);
            preActivate();
            Log.d(TAG, "model activated: " + LDT.id(this));
            publish(ModelEvent.ACTIVATED);
        }
    }

    /**
     * A hook that get called before publishing {@linkplain ModelEvent#ACTIVATED activated} event
     * to the observers.
     * <p>
     * Note that direct calls to this method from subclasses will not mark this instance as
     * activated, instead call {@link #ensureActivated()}
     */
    @ForOverride
    protected void preActivate() {
    }

    /**
     * {@inheritDoc}
     *
     * @return whether this model has been activated
     * (and published {@linkplain ModelEvent#ACTIVATED activated} event) or no
     */
    @Override
    public boolean isActivated() {
        return lifecycle.getCurrentState().isAtLeast(ACTIVATED_STATE);
    }

    /**
     * {@inheritDoc}
     *
     * @return The lifecycle of this model
     */
    @NonNull
    @Override
    @Contract(pure = true)
    public final Lifecycle getLifecycle() {
        return lifecycle;
    }

    /*
     * Returns the canonical name of this class
     * as defined by the Java Language Specification.
     */
    @NonNull
    private String getMyName() {
        return getNameOf(getClass());
    }

    /*
     * Returns the canonical name of the given class
     * as defined by the Java Language Specification.
     */
    @NonNull
    private String getNameOf(@NonNull final Class<? extends StateOwner> runtimeClass) {
        final String name = runtimeClass.getCanonicalName();

        if (name == null)
            throw new RuntimeException(CANONICAL_NAME_MISSING + runtimeClass);

        return name;
    }

    /**
     * Returns the current state of this instance
     *
     * @return current state of this instance
     */
    @NonNull
    @Contract(pure = true)
    protected final Bundle getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public final void saveState(@NonNull final Bundle state) {
        Assert.nonNull(state);
        ensureActivated();
        observers.publish(ModelEvent.SAVING_STATE);
        preStateSave();
        state.putBundle(getMyName(), this.state);
    }

    /**
     * A hook that gets called right <em>before</em> saving the state of this instance and
     * <em>after</em> notifying registered observers
     */
    @ForOverride
    protected void preStateSave() {
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public final void loadState(@Nullable final Bundle state) {
        if (state != null) {
            final Bundle loadedState = state.getBundle(getMyName());

            if (loadedState != null) {
                ensureActivated();

                this.state.putAll(loadedState);
                postStateLoad();
            }

        }
    }

    /**
     * A hook that gets called right <em>after</em> loading new state to this instance and <em>before</em> notifying
     * registered observers
     */
    @ForOverride
    protected void postStateLoad() {
    }

    /**
     * Returns the current exportable state of this instance
     *
     * @return the current exportable state of this instance
     */
    @NonNull
    @Contract(pure = true)
    protected final Bundle getExportableState() {
        return exportableState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void exportState(@NonNull final Intent intent) {
        exportState(intent, getClass());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public final void exportState(@NonNull final Intent intent,
                                  @NonNull final Class<? extends StateOwner> receiver) {
        Assert.nonNull(intent);
        ensureActivated();
        observers.publish(ModelEvent.EXPORTING_STATE);
        preStateExport();
        intent.putExtra(getNameOf(receiver), exportableState);
    }

    /**
     * A hook that gets called right <em>before</em> exporting the state of this instance and <em>after</em> notifying
     * registered observers
     */
    @ForOverride
    protected void preStateExport() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void importState(@Nullable final Intent intent) {
        importState(intent, getClass());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public final void importState(@Nullable final Intent intent,
                                  @NonNull final Class<? extends StateOwner> receiver) {

        if (intent != null) {
            final String receiverName = getNameOf(receiver);
            final String myName = getMyName();

            if (receiverName.equals(myName)) {
                final Bundle importedState = intent.getBundleExtra(myName);

                if (importedState != null) {
                    ensureActivated();
                    state.putAll(importedState);
                    postStateImport();
                    observers.publish(ModelEvent.STATE_IMPORTED);
                }

            }
        }
    }

    /**
     * A hook that gets called right <em>after</em> importing new state to this instance and <em>before</em> notifying
     * registered observers
     */
    @ForOverride
    protected void postStateImport() {
    }

    /**
     * {@inheritDoc}
     * Then call {@link #onRefreshRequested()} to start the actual refreshing
     */
    @Override
    public final void startRefreshing() {
        if (!isRefreshing) {
            markAsRefreshing();
            onRefreshRequested();
        }
    }

    @Override
    @CallSuper
    public void markAsRefreshing() {
        if (!isRefreshing) {
            isRefreshing = true;
            ensureActivated();
            observers.publish(ModelEvent.REFRESHING);
        }
    }

    /**
     * Refreshes the data of this model in an implementation specific way.
     * <p>
     * implementation of this method must make sure that each call to this method will cause a
     * call to {@link #markAsRefreshDone()}, otherwise this model
     * may stay in "refreshing" state forever!
     * <p>
     * The default implementation preform an immediate call to {@code stopRefreshing()}
     */
    @ForOverride
    protected void onRefreshRequested() {
        markAsRefreshDone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAsRefreshDone() {
        if (isRefreshing) {
            isRefreshing = false;
            ensureActivated();
            observers.publish(ModelEvent.REFRESH_DONE);
        }
    }

    /**
     * Dispatches an arbitrary event to observers of this model.
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     *
     * @param event event to be dispatched
     */
    @ForOverride
    protected final void publish(@NonNull final FrontendEvent event) {
        ensureActivated();
        observers.publish(event);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRefreshing() {
        return isRefreshing;
    }

    /**
     * {@inheritDoc}
     */
    public void markAs(boolean refreshing) {
        if (refreshing)
            markAsRefreshing();
        else
            markAsRefreshDone();
    }

    /**
     * Clears the observers list and dispatch DESTROYED state of this instance
     */
    @Override
    @CallSuper
    @ForOverride
    protected void onCleared() {
        if (lifecycle.getCurrentState().isAtLeast(State.CREATED))
        lifecycle.setCurrentState(State.DESTROYED);
        observers.close();
        state.clear();
        exportableState.clear();
    }

    public boolean isClosed() {
        return getLifecycle().getCurrentState() == State.DESTROYED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addModelObserver(@NonNull final MainObserver<FrontendEvent> observer) {
        ensureActivated();

        final EnumSet<ModelEvent> events = EnumSet.noneOf(ModelEvent.class);
        if (isActivated())
            events.add(ModelEvent.ACTIVATED);

        if (isRefreshing())
            events.add(ModelEvent.REFRESHING);

        observers.add(observer, events);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeModelObserver(@NonNull final MainObserver<FrontendEvent> observer) {
        ensureActivated();
        observer.onChanged(ModelEvent.SAVING_STATE);
        observers.remove(observer);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public byte getByte(@NonNull final String key) {
        ensureActivated();
        return state.getByte(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public byte getByte(@NonNull final String key,
                        final byte defaultValue) {
        ensureActivated();
        final Byte aByte = state.getByte(Assert.nonNull(key), defaultValue);
        return Assert.nonNull(aByte); // since Bundle#getByte returns Byte instead of byte (I wounder why just like you)
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public boolean getBoolean(@NonNull final String key) {
        ensureActivated();
        return state.getBoolean(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public boolean getBoolean(@NonNull final String key,
                              final boolean defaultValue) {
        ensureActivated();
        return state.getBoolean(Assert.nonNull(key), defaultValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public char getChar(@NonNull final String key) {
        ensureActivated();
        return state.getChar(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public char getChar(@NonNull final String key,
                        final char defaultValue) {
        ensureActivated();
        return state.getChar(Assert.nonNull(key), defaultValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public short getShort(@NonNull final String key) {
        ensureActivated();
        return state.getShort(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public short getShort(@NonNull final String key,
                          final short defaultValue) {
        ensureActivated();
        return state.getShort(Assert.nonNull(key), defaultValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public float getFloat(@NonNull final String key) {
        ensureActivated();
        return state.getFloat(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public float getFloat(@NonNull final String key,
                          final float defaultValue) {
        ensureActivated();
        return state.getFloat(Assert.nonNull(key), defaultValue);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public CharSequence getCharSequence(@NonNull final String key) {
        ensureActivated();
        return state.getCharSequence(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @NonNull
    @Override
    public CharSequence getCharSequence(@NonNull final String key,
                                        @NonNull final CharSequence defaultValue) {
        ensureActivated();
        final CharSequence charSequence = state.getCharSequence(Assert.nonNull(key), Assert.nonNull(defaultValue));
        return Assert.nonNull(charSequence);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public String getString(@NonNull final String key) {
        ensureActivated();
        return state.getString(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @NonNull
    @Override
    public String getString(@NonNull final String key,
                            @NonNull final String defaultValue) {
        ensureActivated();
        final String string = state.getString(Assert.nonNull(key), Assert.nonNull(defaultValue));
        return Assert.nonNull(string);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public Bundle getBundle(@NonNull final String key) {
        ensureActivated();
        return state.getBundle(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public <T extends Parcelable> T getParcelable(@NonNull final String key) {
        ensureActivated();
        return state.getParcelable(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public Parcelable[] getParcelableArray(@NonNull final String key) {
        ensureActivated();
        return state.getParcelableArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public <T extends Parcelable> ArrayList<T> getParcelableArrayList(@NonNull final String key) {
        ensureActivated();
        return state.getParcelableArrayList(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public <T extends Parcelable> SparseArray<T> getSparseParcelableArray(@NonNull final String key) {
        ensureActivated();
        return state.getSparseParcelableArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public Serializable getSerializable(@NonNull final String key) {
        ensureActivated();
        return state.getSerializable(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public ArrayList<Integer> getIntegerArrayList(@NonNull final String key) {
        ensureActivated();
        return state.getIntegerArrayList(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public ArrayList<String> getStringArrayList(@NonNull final String key) {
        ensureActivated();
        return state.getStringArrayList(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public ArrayList<CharSequence> getCharSequenceArrayList(@NonNull final String key) {
        ensureActivated();
        return state.getCharSequenceArrayList(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public byte[] getByteArray(@NonNull final String key) {
        ensureActivated();
        return state.getByteArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public short[] getShortArray(@NonNull final String key) {
        ensureActivated();
        return state.getShortArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public char[] getCharArray(@NonNull final String key) {
        ensureActivated();
        return state.getCharArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public float[] getFloatArray(@NonNull final String key) {
        ensureActivated();
        return state.getFloatArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Nullable
    @Override
    public CharSequence[] getCharSequenceArray(@NonNull final String key) {
        ensureActivated();
        return state.getCharSequenceArray(Assert.nonNull(key));
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putByte(@NonNull final String key,
                        final byte value) {
        ensureActivated();
        state.putByte(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putBoolean(@NonNull final String key,
                           final boolean value) {
        ensureActivated();
        state.putBoolean(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putChar(@NonNull final String key,
                        final char value) {
        ensureActivated();
        state.putChar(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putShort(@NonNull final String key,
                         final short value) {
        ensureActivated();
        state.putShort(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putFloat(@NonNull final String key,
                         final float value) {
        ensureActivated();
        state.putFloat(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putCharSequence(@NonNull final String key,
                                @Nullable final CharSequence value) {
        ensureActivated();
        state.putCharSequence(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putParcelable(@NonNull final String key,
                              @Nullable final Parcelable value) {
        ensureActivated();
        state.putParcelable(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putParcelableArray(@NonNull final String key,
                                   @Nullable final Parcelable[] value) {
        ensureActivated();
        state.putParcelableArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putParcelableArrayList(@NonNull final String key,
                                       @Nullable final ArrayList<? extends Parcelable> value) {
        ensureActivated();
        state.putParcelableArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putSparseParcelableArray(@NonNull final String key,
                                         @Nullable final SparseArray<? extends Parcelable> value) {
        ensureActivated();
        state.putSparseParcelableArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putIntegerArrayList(@NonNull final String key,
                                    @Nullable final ArrayList<Integer> value) {
        ensureActivated();
        state.putIntegerArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putStringArrayList(@NonNull final String key,
                                   @Nullable final ArrayList<String> value) {
        ensureActivated();
        state.putStringArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putCharSequenceArrayList(@NonNull final String key,
                                         @Nullable final ArrayList<CharSequence> value) {
        ensureActivated();
        state.putCharSequenceArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putSerializable(@NonNull final String key,
                                @Nullable final Serializable value) {
        ensureActivated();
        state.putSerializable(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putByteArray(@NonNull final String key,
                             @Nullable final byte[] value) {
        ensureActivated();
        state.putByteArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putShortArray(@NonNull final String key,
                              @Nullable final short[] value) {
        ensureActivated();
        state.putShortArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putCharArray(@NonNull final String key,
                             @Nullable final char[] value) {
        ensureActivated();
        state.putCharArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putFloatArray(@NonNull final String key,
                              @Nullable final float[] value) {
        ensureActivated();
        state.putFloatArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putCharSequenceArray(@NonNull final String key,
                                     @Nullable final CharSequence[] value) {
        ensureActivated();
        state.putCharSequenceArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putString(@NonNull final String key,
                          @Nullable final String value) {
        ensureActivated();
        state.putString(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void putBundle(@NonNull final String key,
                          @Nullable final Bundle value) {
        ensureActivated();
        state.putBundle(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportByte(@NonNull final String key,
                           final byte value) {
        ensureActivated();
        exportableState.putByte(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportBoolean(@NonNull final String key,
                              final boolean value) {
        ensureActivated();
        exportableState.putBoolean(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportChar(@NonNull final String key,
                           final char value) {
        ensureActivated();
        exportableState.putChar(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportShort(@NonNull final String key,
                            final short value) {
        ensureActivated();
        exportableState.putShort(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportFloat(@NonNull final String key,
                            final float value) {
        ensureActivated();
        exportableState.putFloat(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportCharSequence(@NonNull final String key,
                                   @Nullable final CharSequence value) {
        ensureActivated();
        exportableState.putCharSequence(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportParcelable(@NonNull final String key,
                                 @Nullable final Parcelable value) {
        ensureActivated();
        exportableState.putParcelable(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportParcelableArray(@NonNull final String key,
                                      @Nullable final Parcelable[] value) {
        ensureActivated();
        exportableState.putParcelableArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportParcelableArrayList(@NonNull final String key,
                                          @Nullable final ArrayList<? extends Parcelable> value) {
        ensureActivated();
        exportableState.putParcelableArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportSparseParcelableArray(@NonNull final String key,
                                            @Nullable final SparseArray<? extends Parcelable> value) {
        ensureActivated();
        exportableState.putSparseParcelableArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportIntegerArrayList(@NonNull final String key,
                                       @Nullable final ArrayList<Integer> value) {
        ensureActivated();
        exportableState.putIntegerArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportStringArrayList(@NonNull final String key,
                                      @Nullable final ArrayList<String> value) {
        ensureActivated();
        exportableState.putStringArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportCharSequenceArrayList(@NonNull final String key,
                                            @Nullable final ArrayList<CharSequence> value) {
        ensureActivated();
        exportableState.putCharSequenceArrayList(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportString(@NonNull final String key,
                             @Nullable final String value) {
        ensureActivated();
        exportableState.putString(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportSerializable(@NonNull final String key,
                                   @Nullable final Serializable value) {
        ensureActivated();
        exportableState.putSerializable(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportByteArray(@NonNull final String key,
                                @Nullable final byte[] value) {
        ensureActivated();
        exportableState.putByteArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportShortArray(@NonNull final String key,
                                 @Nullable final short[] value) {
        ensureActivated();
        exportableState.putShortArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportCharArray(@NonNull final String key,
                                @Nullable final char[] value) {
        ensureActivated();
        exportableState.putCharArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportFloatArray(@NonNull final String key,
                                 @Nullable final float[] value) {
        ensureActivated();
        exportableState.putFloatArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportCharSequenceArray(@NonNull final String key,
                                        @Nullable final CharSequence[] value) {
        ensureActivated();
        exportableState.putCharSequenceArray(Assert.nonNull(key), value);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method may {@linkplain ModelEvent#ACTIVATED activated} this model if it
     * is not already {@linkplain #isActivated() activated}
     */
    @Override
    public void exportBundle(@NonNull final String key,
                             @Nullable final Bundle value) {
        ensureActivated();
        exportableState.putBundle(Assert.nonNull(key), value);
    }
}
