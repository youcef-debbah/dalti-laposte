package dz.jsoftware95.silverbox.android.concurrent;

import android.annotation.SuppressLint;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.silverbox.android.common.Check;

/**
 * A Job that has access to a single external context.
 *
 * <p>
 * the context object will be available as a parameter
 * to the execution hooks ({@link #doFromMain(Object)},
 * {@link #doFromBackground(Object)}, {@link #closeFromMain(Object)}
 * and {@link #closeFromBackground(Object)}). The context will get nulled
 * when this Job instance {@linkplain #isClosed() is closed}
 * </p>
 *
 * @param <C> the type of the context
 */
@AnyThread
public abstract class UnJob<C> extends AbstractJob {

    @NonNull
    private final AtomicReference<C> contextHolder;

    /**
     * Constructs a new Job instance that is backed by the
     * {@linkplain SystemWorker#MAIN main worker}
     * and not {@linkplain #isReusable() reusable}.
     *
     * @param context the context instance that will be available as a parameter
     *                to the execution hooks ({@link #doFromMain(Object)},
     *                {@link #doFromBackground(Object)}, {@link #closeFromMain(Object)}
     *                and {@link #closeFromBackground(Object)})
     */
    protected UnJob(@NonNull final C context) {
        super(SystemWorker.MAIN, false);
        this.contextHolder = new AtomicReference<>(Check.nonNull(context));
    }

    /**
     * Constructs a new Job instance that is backed by the
     * {@linkplain SystemWorker#MAIN main worker}.
     *
     * @param context  the context instance that will be available as a parameter
     *                 to the execution hooks ({@link #doFromMain(Object)},
     *                 {@link #doFromBackground(Object)}, {@link #closeFromMain(Object)}
     *                 and {@link #closeFromBackground(Object)})
     * @param reusable whether the new Job instance is reusable
     */
    protected UnJob(@NonNull final C context, final boolean reusable) {
        super(SystemWorker.MAIN, reusable);
        this.contextHolder = new AtomicReference<>(Check.nonNull(context));
    }

    /**
     * Constructs a new Job instance that not {@linkplain #isReusable() reusable}.
     *
     * @param worker  the executor that encapsulates the default thread of the new Job
     * @param context the context instance that will be available as a parameter
     *                to the execution hooks ({@link #doFromMain(Object)},
     *                {@link #doFromBackground(Object)}, {@link #closeFromMain(Object)}
     *                and {@link #closeFromBackground(Object)})
     */
    protected UnJob(@NonNull final JobWorker worker,
                    @NonNull final C context) {
        super(Check.nonNull(worker), false);
        this.contextHolder = new AtomicReference<>(Check.nonNull(context));
    }

    /**
     * Constructs a new Job instance.
     *
     * @param worker   the executor that encapsulates the default thread of the new Job instance
     * @param context  the context instance that will be available as a parameter
     *                 to the execution hooks ({@link #doFromMain(Object)},
     *                 {@link #doFromBackground(Object)}, {@link #closeFromMain(Object)}
     *                 and {@link #closeFromBackground(Object)})
     * @param reusable whether the new Job instance is reusable
     */
    protected UnJob(@NonNull final JobWorker worker,
                    @NonNull final C context,
                    final boolean reusable) {
        super(Check.nonNull(worker), reusable);
        this.contextHolder = new AtomicReference<>(Check.nonNull(context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isClosed() {
        return contextHolder.get() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run() {
        final C context;
        final boolean closing;
        if (isReusable()) {
            context = contextHolder.get();
            closing = false;
        } else {
            context = contextHolder.getAndSet(null);
            closing = context != null; // if context is null then the job has been already closed so it's not "closing"
        }

        if (context != null)
            callDoHook(context, closing);
    }

    @SuppressLint("WrongThread")
    private void callDoHook(final C context, final boolean closeWhenDone) {
        RuntimeException mainException = null;
        try {
            if (SystemWorker.MAIN.isCurrentThread())
                doFromMain(context);
            else
                doFromBackground(context);
        } catch (RuntimeException e) {
            throw mainException = e;
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } finally {
            if (closeWhenDone) {
                if (mainException != null)
                    try {
                        callCloseHook(context);
                    } catch (RuntimeException e) {
                        mainException.addSuppressed(e);
                    }
                else
                    callCloseHook(context);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        final C context = contextHolder.getAndSet(null);
        if (context != null)
            callCloseHook(context);
    }

    @SuppressLint("WrongThread")
    private void callCloseHook(final C context) {
        if (SystemWorker.MAIN.isCurrentThread())
            closeFromMain(context);
        else
            closeFromBackground(context);
    }

    /**
     * A hook method that gets called when this Job is
     * executed by the main thread.
     * <p>
     * If this Job is executed by any other thread then
     * {@link #doFromBackground(Object)} is called instead.
     * </p>
     * <p>
     * Note that default implementation of this method throws an
     * UnsupportedOperationException to reduce the chances of
     * a subclass in implementing {@link #doFromBackground(Object)}
     * where it should have overridden this method
     * </p>
     *
     * @param context the context that was given at construction time
     */
    @MainThread
    protected void doFromMain(@NonNull final C context) {
        methodNotImplemented("doFromMain(context)");
    }

    /**
     * A hook method that gets called when this Job instance is
     * executed by a background thread.
     * <p>
     * If this Job is executed by the main thread then
     * {@link #doFromMain(Object)} is called instead.
     * </p>
     * <p>
     * Note that default implementation of this method throws an
     * UnsupportedOperationException to reduce the chances of
     * a subclass in implementing {@link #doFromMain(Object)}
     * where it should have overridden this method
     * </p>
     *
     * @param context the context that was given at construction time
     */
    @WorkerThread
    protected void doFromBackground(@NonNull final C context) throws InterruptedException {
        methodNotImplemented("doFromBackground(context)");
    }

    /**
     * A hook method that gets called when this Job is being
     * closed from the main thread.
     *
     * @param context the context that was given at construction time
     */
    @MainThread
    protected void closeFromMain(@NonNull final C context) {
    }

    /**
     * A hook method that gets called when this Job is being
     * closed from a background thread.
     *
     * @param context the context that was given at construction time
     */
    @WorkerThread
    protected void closeFromBackground(@NonNull final C context) {
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString() + "{" +
                "context=" + contextHolder.get() +
                '}';
    }
}
