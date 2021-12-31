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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.concurrent.NotThreadSafe;

import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.concurrent.JobWorker;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;

/**
 * Log and Debug Toolkit, a utility class that encapsulates redundant code that is related to
 * writing log/debug-info to the output, in a way that is configurable by {@link Config} class.
 */
@NotThreadSafe
public final class LDT {

    private static final String TAG = "silverbox_log";
    private static final String SEPARATOR = "----------------------------------------";

    private LDT() {
        throw new UnsupportedOperationException("bad boy! no instance for you");
    }

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of seconds, subject to
     * the precision and accuracy of system timers and schedulers. The thread
     * does not lose ownership of any monitors.
     * <p>
     * In case the sleeping thread has been interrupted the interruption will
     * be {@link #logInterruption()} logged}
     * </P>
     *
     * @param seconds the length of time to sleep in seconds
     * @throws IllegalArgumentException if the value of {@code seconds} is negative
     */
    public static void sleep(@IntRange(from = 0) final int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            logInterruption("sleeping for: " + seconds + " sec", e);
        }
    }

    /**
     * Causes the currently executing thread to sleep for a long time
     * (more than 292 million year), The thread does not lose ownership of any monitors.
     * <p>
     * In case the sleeping thread has been interrupted the interruption will
     * be {@link #logInterruption()} logged}
     * </P>
     *
     * @throws IllegalArgumentException if the value of {@code seconds} is negative
     */
    public static void sleepForever() {
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            logInterruption("sleeping forever...", e);
        }
    }

    @Contract("_ -> param1")
    public static <T> T peek(final T instance) {
        return peek("peeking", instance);
    }

    /**
     * Writes the {@linkplain Represent#COMPACTLY compact representation} of a
     * given {@code instance} to an error log message.
     * <p>
     * Log message format: {@code prefix + ": " + instanceRepresentation}
     * </p>
     *
     * @param prefix   the prefix of the log message
     * @param instance the instance to be logged
     * @param <T>      the type of the instance to be logged
     * @return the given {@code instance}
     */
    @Contract("_, _ -> param2")
    public static <T> T peek(@Nullable final String prefix, final T instance) {
        e(prefix + ": " + Represent.COMPACTLY.asString(instance));
        return instance;
    }

    /**
     * Returns a string representation of the object.
     *
     * @param instance the instance to be represented
     * @return the given {@code instance}
     */
    @NonNull
    @Contract(pure = true)
    public static String asString(@Nullable final Object instance) {
        return Represent.the(instance);
    }

    /**
     * Returns hash code of the given {@code instance} as a hex string, or {@code "null"}
     * if the {@code instance} is {@code null}.
     *
     * @param instance the {@code instance} to be represented
     * @return the given {@code instance}
     */
    @NonNull
    public static String id(@Nullable final Object instance) {
        if (instance == null)
            return "null";
        else
            return "0x" + Integer.toHexString(instance.hashCode());
    }

    /**
     * Returns the suffix (after the last dot) of the fully qualified name
     * of the class of the given {@code instance}.
     * <p>
     * Note that the returned name may be different that the
     * {@linkplain Class#getSimpleName() class simple name})
     * </P>
     *
     * @param instance the {@code instance} to be represented
     * @return a short name of the {@code instance} class
     */
    @NonNull
    public static String shortTypeName(@NonNull final Object instance) {
        final String name = instance.getClass().getName();
        return name.substring(name.lastIndexOf('.') + 1);

    }

    /**
     * Returns a string representation similar to the one returned by the default
     * implementation of {@linkplain Object#toString() toString()} in the {@code Object} class
     * , or {@code "null"} if the given {@code instance} is {@code null}.
     * <p>
     * Instead of using the {@linkplain Class#getName() class name} like the actual
     * default implementation of {@linkplain Object#toString() toString()} the
     * {@linkplain #shortTypeName(Object)} class short name} is used instead
     * </p>
     *
     * @param instance the {@code instance} to be represented
     * @return the string representation of the given {@code instance}
     */
    @NonNull
    public static String instanceName(@Nullable final Object instance) {
        if (instance == null)
            return "null";
        else
            return shortTypeName(instance) + "@" + Integer.toHexString(instance.hashCode());
    }

    /**
     * Send a {@link Log#VERBOSE} log message if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     */
    public static void v(@Nullable final Object message) {
        if (Config.LOG_DEBUG_INFO)
            Log.v(TAG, asString(message));
    }

    /**
     * Send a {@link Log#VERBOSE} log message and log the exception
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     * @param e       An exception to log
     */
    public static void v(@Nullable final Object message,
                         @Nullable final Throwable e) {
        if (Config.LOG_DEBUG_INFO)
            Log.v(TAG, asString(message), e);
    }

    /**
     * Send a {@link Log#DEBUG} log message if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     */
    public static void d(@Nullable final Object message) {
        if (Config.LOG_DEBUG_INFO)
            Log.d(TAG, asString(message));
    }

    /**
     * Send a {@link Log#DEBUG} log message and log the exception
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     * @param e       An exception to log
     */
    public static void d(@Nullable final Object message,
                         @Nullable final Throwable e) {
        if (Config.LOG_DEBUG_INFO)
            Log.d(TAG, asString(message), e);
    }

    /**
     * Send a {@link Log#INFO} log message if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     */
    public static void i(@Nullable final Object message) {
        if (Config.LOG_DEBUG_INFO)
            Log.i(TAG, asString(message));
    }

    /**
     * Send a {@link Log#INFO} log message and log the exception
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     * @param e       An exception to log
     */
    public static void i(@Nullable final Object message,
                         @Nullable final Throwable e) {
        if (Config.LOG_DEBUG_INFO)
            Log.i(TAG, asString(message), e);
    }

    /**
     * Send a {@link Log#WARN} log message if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     */
    public static void w(@Nullable final Object message) {
        if (Config.LOG_DEBUG_INFO)
            Log.w(TAG, asString(message));
    }

    /**
     * Send a {@link Log#WARN} log message and log the exception
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     * @param e       An exception to log
     */
    public static void w(@Nullable final Object message,
                         @Nullable final Throwable e) {
        if (Config.LOG_DEBUG_INFO)
            Log.w(TAG, asString(message), e);
    }

    /**
     * Send a {@link Log#ERROR} log message if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     */
    public static void e(@Nullable final Object message) {
        if (Config.LOG_DEBUG_INFO)
            Log.e(TAG, asString(message));
    }

    /**
     * Send a {@link Log#ERROR} log message and log the exception
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     *
     * @param message The message you would like logged.
     * @param e       An exception to log
     */
    public static void e(@Nullable final String message,
                         @Nullable final Throwable e) {
        if (Config.LOG_DEBUG_INFO)
            Log.e(TAG, asString(message), e);
    }

    /**
     * Send an {@linkplain Log#INFO info} log message containing only
     * the default {@linkplain #SEPARATOR log separator}
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     */
    public static void infoSeparator() {
        i(SEPARATOR);
    }

    /**
     * Send an {@linkplain Log#ERROR error} log message containing only
     * the default {@linkplain #SEPARATOR log separator}
     * if {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled, using {@linkplain #TAG debug tag}.
     */
    public static void errorSeparator() {
        e(SEPARATOR);
    }


    /**
     * Interrupts the current thread and (in case {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled) log a warning message containing the name of the current thread.
     */
    public static void logInterruption() {
        final Thread thread = Thread.currentThread();
        if (!SystemWorker.MAIN.isCurrentThread())
            thread.interrupt();
        if (Config.LOG_DEBUG_INFO)
            w("thread '" + thread.getName() + "' interrupted");
    }

    /**
     * Interrupts the current thread and (in case {@linkplain Config#LOG_DEBUG_INFO debug log}
     * is enabled) log a warning message containing the name of the current thread and suffixed
     * by the given {@code interruptedWhile} String.
     * <p>
     * Log message format: {@code "thread '" + threadName + "' interrupted while " + interruptedWhile}
     * </p>
     *
     * @param interruptedWhile a description of what the task that was on-going when
     *                         the interruption has occurred
     * @param e                the interrupted exception
     */
    public static void logInterruption(@Nullable final String interruptedWhile,
                                       @Nullable final InterruptedException e) {
        final Thread thread = Thread.currentThread();
        thread.interrupt();
        if (Config.LOG_DEBUG_INFO)
            w("thread '" + thread.getName() + "' interrupted while " + interruptedWhile, e);
    }

    @NotNull
    public static Lock lockToWrite(ReadWriteLock readWriteLock, String operation) {
        long t0 = System.nanoTime();
        Log.d(TAG, operation + ".writeLock.lock {");
        final Lock writeLock = readWriteLock.writeLock();
        writeLock.lock();
        Log.d(TAG, operation + ".writeLock.lock } in:" + GlobalUtil.millisSince(t0) + " ms");
        return writeLock;
    }

    @NotNull
    public static Lock lockToRead(ReadWriteLock readWriteLock, String operation) {
        long t0 = System.nanoTime();
        Log.d(TAG, operation + ".readLock.lock {");
        final Lock lock = readWriteLock.readLock();
        lock.lock();
        Log.d(TAG, operation + ".readLock.lock } in: " + GlobalUtil.millisSince(t0) + " ms");
        return lock;
    }

    public static void await(@NonNull CountDownLatch latch, int secondsTimeout, String operation, JobWorker worker) throws InterruptedException {
        Check.isNotMainThread("you can't wait on a CountDownLatch from the main thread");
        Check.not(worker.isCurrentThread(), "dead lock detected, " + worker + " thread was trying to wait on a CountDownLatch that it is supposed to trigger!");
        long t0 = System.nanoTime();
        Log.d(TAG, operation + ".latch.await {");
        try {
            latch.await(secondsTimeout, TimeUnit.SECONDS);
            Log.d(TAG, operation + ".latch.await } in: " + GlobalUtil.millisSince(t0) + " ms");
        } catch (InterruptedException e) {
            Log.e(TAG, operation + ".latch.await interrupted in: " + GlobalUtil.millisSince(t0) + " ms", e);
            throw e;
        }
    }
}