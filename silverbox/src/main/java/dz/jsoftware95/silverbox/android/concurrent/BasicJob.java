package dz.jsoftware95.silverbox.android.concurrent;

import android.annotation.SuppressLint;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.concurrent.atomic.AtomicBoolean;

import dz.jsoftware95.silverbox.android.common.Check;

/**
 * A basic Job implementation that doesn't hold any closable reference to
 * external contexts.
 */
@AnyThread
public abstract class BasicJob extends AbstractJob {

    private final AtomicBoolean isClosed = new AtomicBoolean();

    /**
     * Constructs a new Job instance that is backed by the
     * {@linkplain SystemWorker#MAIN main worker}
     * and not {@linkplain #isReusable() reusable}.
     */
    protected BasicJob() {
        super(SystemWorker.MAIN, false);
    }

    /**
     * Constructs a new Job instance that is backed by the
     * {@linkplain SystemWorker#MAIN main worker}.
     *
     * @param reusable whether the new Job instance is reusable
     */
    protected BasicJob(final boolean reusable) {
        super(SystemWorker.MAIN, reusable);
    }

    /**
     * Constructs a new Job instance that not {@linkplain #isReusable() reusable}.
     *
     * @param worker the executor that encapsulates the default thread of the new Job
     */
    protected BasicJob(@NonNull final JobWorker worker) {
        super(Check.nonNull(worker), false);
    }

    /**
     * Constructs a new Job instance.
     *
     * @param worker   the executor that encapsulates the default thread of the new Job instance
     * @param reusable whether the new Job instance is reusable
     */
    protected BasicJob(@NonNull final JobWorker worker,
                       final boolean reusable) {
        super(Check.nonNull(worker), reusable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isClosed() {
        return isClosed.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run() {
        if (isReusable()) {
            if (!isClosed.get())
                callDoHook(false);
        } else if (!isClosed.getAndSet(true))
            callDoHook(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        final boolean wasClosed = isClosed.getAndSet(true);
        if (!wasClosed)
            callCloseHook();
    }

    @SuppressLint("WrongThread")
    private void callDoHook(final boolean closeWhenDone) {
        RuntimeException mainException = null;
        try {
            if (SystemWorker.MAIN.isCurrentThread())
                doFromMain();
            else
                doFromBackground();
        } catch (RuntimeException e) {
            throw mainException = e;
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } finally {
            if (closeWhenDone) {
                if (mainException != null)
                    try {
                        callCloseHook();
                    } catch (RuntimeException e) {
                        mainException.addSuppressed(e);
                    }
                else
                    callCloseHook();
            }
        }
    }

    @SuppressLint("WrongThread")
    private void callCloseHook() {
        if (SystemWorker.MAIN.isCurrentThread())
            closeFromMain();
        else
            closeFromBackground();
    }

    /**
     * A hook method that gets called when this Job is
     * executed by the main thread.
     * <p>
     * If this Job is executed by any other thread then
     * {@link #doFromBackground()} is called instead.
     * </p>
     * <p>
     * Note that default implementation of this method throws an
     * UnsupportedOperationException to reduce the chances of
     * a subclass in implementing {@link #doFromBackground()}
     * where it should have overridden this method
     * </p>
     */
    @MainThread
    protected void doFromMain() {
        methodNotImplemented("doFromMain()");
    }

    /**
     * A hook method that gets called when this Job instance is
     * executed by a background thread.
     * <p>
     * If this Job is executed by the main thread then
     * {@link #doFromMain()} is called instead.
     * </p>
     * <p>
     * Note that default implementation of this method throws an
     * UnsupportedOperationException to reduce the chances of
     * a subclass in implementing {@link #doFromMain()}
     * where it should have overridden this method
     * </p>
     */
    @WorkerThread
    protected void doFromBackground() throws InterruptedException {
        methodNotImplemented("doFromBackground()");
    }

    /**
     * A hook method that gets called when this Job is being
     * closed from the main thread.
     */
    @MainThread
    protected void closeFromMain() {
    }

    /**
     * A hook method that gets called when this Job is being
     * closed from a background thread.
     */
    @WorkerThread
    protected void closeFromBackground() {
    }
}
