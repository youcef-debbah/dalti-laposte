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

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import dagger.Lazy;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.LDT;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.ClosableQueue;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;

@AnyThread
public abstract class LazyRepository<DAO> extends AbstractRepository {

    public static final int PENDING_JOB_TIMEOUT = 20;

    private final AtomicReference<Lazy<DAO>> daoSupplier;
    private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
    private final ClosableQueue<Job> pendingJobs = new ClosableQueue<>();
    private volatile DAO dao;
    private final CountDownLatch initLatch = new CountDownLatch(1);

    @AnyThread
    public LazyRepository(Lazy<DAO> daoSupplier) {
        this.daoSupplier = new AtomicReference<>(daoSupplier);
    }

    /**
     * non blocking
     */
    @AnyThread
    public final boolean isInitialized() {
        return this.initLatch.getCount() == 0;
    }

    public final boolean isDataAvailable() {
        return dao != null;
    }

    @WorkerThread
    public void initialize(@NonNull final DAO dao) throws InterruptedException {
        Objects.requireNonNull(dao);
        if (!isInitialized()) {
            this.daoSupplier.set(null);
            final Lock writeLock = LDT.lockToWrite(stateLock, "initialize");
            long now = System.nanoTime();
            if (!isInitialized())
                try {
                    this.dao = dao;
                    ontInitialize();
                } finally {
                    try {
                        pendingJobs.close(Job::execute);
                    } finally {
                        this.initLatch.countDown();
                        writeLock.unlock();
                        Log.i(TAG, getClass().getSimpleName() + ".initialize() done in:" + GlobalUtil.millisSince(now) + " ms");
                    }
                }
        }
    }

    @WorkerThread
    @SuppressWarnings("RedundantThrows")
    protected void ontInitialize() throws InterruptedException {
    }

    public void initializeIfNeeded() {
        Lazy<DAO> daoSupplier = this.daoSupplier.get();
        if (daoSupplier != null) {
            Job initJob = newInitJob(this, daoSupplier);
            if (AppWorker.DATABASE.isCurrentThread())
                initJob.run();
            else
                initJob.execute();
        }
    }

    private static <T> Job newInitJob(LazyRepository<T> repository, Lazy<T> daoSupplier) {
        return new DuoDatabaseJob<LazyRepository<T>, Lazy<T>>(repository, daoSupplier) {
            @Override
            protected void doFromBackground(@NotNull LazyRepository<T> repository, @NotNull Lazy<T> daoSupplier) throws InterruptedException {
                repository.initialize(daoSupplier.get());
            }
        };
    }

    public void executeIfPossible(@NonNull final Job job) {
        if (!pendingJobs.queueIfPossible(job))
            executeNow(job);
    }

    /**
     * non blocking
     */
    public void execute(@NonNull final Job job) {
        if (pendingJobs.queueIfPossible(job))
            initializeIfNeeded();
        else
            executeNow(job);
    }

    private void executeNow(@NotNull Job job) {
        Lock readLock = LDT.lockToRead(stateLock, "execute");
        try {
            job.execute();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * blocking
     */
    public void executeAndWait(@NonNull final Job job) throws InterruptedException {
        Check.thatCurrentThreadIn(AppWorker.NETWORK, AppWorker.SYNC);
        final CountDownLatch latch = new CountDownLatch(1);

        execute(new DuoJob<Runnable, CountDownLatch>(job.getWorker(), job, latch) {

            @Override
            protected void doFromBackground(@NonNull final Runnable runnable, @NonNull final CountDownLatch latch) {
                runnable.run();
            }

            @Override
            protected void closeFromBackground(@NonNull final Runnable runnable, @NonNull final CountDownLatch latch) {
                latch.countDown();
            }
        });

        LDT.await(latch, PENDING_JOB_TIMEOUT, "executeAndWait", job.getWorker());
    }

    @NonNull
    @AnyThread
    public DAO requireDAO() {
        Check.thatCurrentThreadNotIn(AppWorker.NETWORK, AppWorker.SYNC);
        return Objects.requireNonNull(dao);
    }

    @WorkerThread
    private void awaitPendingDatabaseJobs(@NonNull String operation) throws InterruptedException {
        Check.thatCurrentThreadIn(AppWorker.NETWORK, AppWorker.SYNC);
        Objects.requireNonNull(operation);
        CountDownLatch latch = new CountDownLatch(1);
        execute(new UnDatabaseJob<CountDownLatch>(latch) {
            @Override
            protected void doFromBackground(@NonNull @NotNull CountDownLatch latch) {
                latch.countDown();
            }
        });
        LDT.await(latch, PENDING_JOB_TIMEOUT, operation, AppWorker.DATABASE);
    }

    @NonNull
    @WorkerThread
    public DAO waitForDAO(String operation) throws InterruptedException {
        awaitPendingDatabaseJobs(operation);
        return Objects.requireNonNull(this.dao);
    }
}
