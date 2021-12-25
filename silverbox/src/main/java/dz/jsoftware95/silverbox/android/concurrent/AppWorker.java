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

package dz.jsoftware95.silverbox.android.concurrent;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;

import androidx.annotation.AnyThread;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dz.jsoftware95.silverbox.android.common.Check;

/**
 * An enum of JobWorker implementations such that each instance is
 * backed by private threads that is started lazily when needed.
 */
@AnyThread
public enum AppWorker implements JobWorker {

    /**
     * A JobWorker that is responsible for executing Database-related tasks in a background thread.
     */
    DATABASE(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE * 2),

    /**
     * A JobWorker that is responsible for executing Database-Logging tasks in a background thread.
     */
    LOG(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_MORE_FAVORABLE),

    /**
     * A JobWorker that is responsible for executing short tasks in a background thread.
     */
    BACKGROUND(Process.THREAD_PRIORITY_BACKGROUND),

    /**
     * A JobWorker that is responsible for executing Network-related tasks in a background thread.
     */
    NETWORK(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE),

    /**
     * A JobWorker that is responsible for executing long background synchronization tasks in a background thread.
     */
    SYNC(Process.THREAD_PRIORITY_BACKGROUND + Process.THREAD_PRIORITY_LESS_FAVORABLE * 2),
    ;

    private static final String THREAD_NAME_SUFFIX = "-HandlerThread";
    private static final String WORKER_NAME_SUFFIX = "-AppWorker";

    @NotNull
    private transient volatile HandlerThread thread;

    /**
     * @serial The priority of the thread that back this worker.
     * The value supplied must be from {@link Process}
     * and not from {@link Thread}.
     */
    private final int priority;

    private final Object initMutex = new Mutex();

    @Nullable
    @GuardedBy("initMutex")
    private transient volatile Handler handler = null;

    AppWorker(final int priority) {
        this.thread = new HandlerThread(name() + THREAD_NAME_SUFFIX, priority);
        this.priority = priority;
    }

    /**
     * Serialize this instance.
     *
     * @param outputStream serialization output stream
     * @throws IOException if I/O errors occur while writing to the underlying
     *                     <var>OutputStream</var>
     * @serialData Default fields
     */
    private void writeObject(final ObjectOutputStream outputStream) throws IOException {
        outputStream.defaultWriteObject();
    }

    /**
     * Deserialize this instance.
     *
     * @param inputStream serialization input stream
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if the class of a serialized object
     *                                could not be found.
     * @serialData Default fields
     */
    private void readObject(final ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        final int priority = this.priority;
        Check.that(priority >= Process.THREAD_PRIORITY_URGENT_AUDIO);
        Check.that(priority <= Process.THREAD_PRIORITY_LOWEST);

        this.thread = new HandlerThread(name() + THREAD_NAME_SUFFIX, priority);
        this.handler = null;
    }

    @NonNull
    private Looper getLooper() {
        final HandlerThread thread = this.thread;
        if (!thread.isAlive())
            synchronized (initMutex) {
                if (!thread.isAlive())
                    thread.start();
            }

        return Check.nonNull(thread.getLooper());
    }

    @NonNull
    @Contract(pure = true)
    private Handler getHandler() {
        Handler handler = this.handler;
        if (handler == null)
            synchronized (initMutex) {
                handler = this.handler;
                if (handler == null) {
                    handler = new Handler(getLooper());
                    this.handler = handler;
                }
            }

        return handler;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean isCurrentThread() {
        return Thread.currentThread().equals(thread);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt() {
        thread.interrupt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBackedByMainThread() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    @Contract(pure = true)
    public String getName() {
        return name() + WORKER_NAME_SUFFIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NonNull final Runnable runnable) {
        getHandler().post(Check.nonNull(runnable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeDelayed(@NonNull final Runnable runnable,
                               final long delayMillis) {
        if (delayMillis > 0)
            getHandler().postDelayed(Check.nonNull(runnable), delayMillis);
        else
            execute(runnable);
    }

    /**
     * {@inheritDoc}
     */
    public void executeAndWait(@NonNull final Job job) throws InterruptedException {
        JobWorker.runAndWait(job);
    }
}
