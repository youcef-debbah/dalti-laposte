package dz.jsoftware95.silverbox.android.concurrent;

import android.annotation.SuppressLint;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.silverbox.android.common.Check;

/**
 * A Job that has access to two external context objects.
 *
 * <p>
 * the context objects will be available as a parameters
 * to the execution hooks ({@link #doFromMain(Object, Object)},
 * {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
 * and {@link #closeFromBackground(Object, Object)}). The context will get nulled
 * when this Job instance {@linkplain #isClosed() is closed}
 * </p>
 *
 * @param <C1> the type of the first context
 * @param <C2> the type of the second context
 */
@AnyThread
public abstract class DuoJob<C1, C2> extends AbstractJob {

    @NonNull
    private final AtomicReference<Contexts<C1, C2>> contextsHolder;

    /**
     * Constructs a new Job instance that is backed by the
     * {@linkplain SystemWorker#MAIN main worker}
     * and not {@linkplain #isReusable() reusable}.
     *
     * @param context1 the first context instance that will be available as the first parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     * @param context2 the second context instance that will be available as the second parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     */
    protected DuoJob(@NonNull final C1 context1, @NonNull final C2 context2) {
        super(SystemWorker.MAIN, false);
        contextsHolder = new AtomicReference<>(new Contexts<>(context1, context2));
    }

    /**
     * Constructs a new Job instance that is backed by the
     * {@linkplain SystemWorker#MAIN main worker}.
     *
     * @param context1 the first context instance that will be available as the first parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     * @param context2 the second context instance that will be available as the second parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     * @param reusable whether the new Job instance is reusable
     */
    protected DuoJob(@NonNull final C1 context1, @NonNull final C2 context2,
                     final boolean reusable) {
        super(SystemWorker.MAIN, reusable);
        contextsHolder = new AtomicReference<>(new Contexts<>(context1, context2));
    }

    /**
     * Constructs a new Job instance that not {@linkplain #isReusable() reusable}.
     *
     * @param worker   the executor that encapsulates the default thread of the new Job
     * @param context1 the first context instance that will be available as the first parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     * @param context2 the second context instance that will be available as the second parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     */
    protected DuoJob(@NonNull final JobWorker worker,
                     @NonNull final C1 context1, @NonNull final C2 context2) {
        super(Check.nonNull(worker), false);
        contextsHolder = new AtomicReference<>(new Contexts<>(context1, context2));
    }

    /**
     * Constructs a new Job instance.
     *
     * @param worker   the executor that encapsulates the default thread of the new Job instance
     * @param context1 the first context instance that will be available as the first parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     * @param context2 the second context instance that will be available as the second parameter
     *                 to the execution hooks ({@link #doFromMain(Object, Object)},
     *                 {@link #doFromBackground(Object, Object)}, {@link #closeFromMain(Object, Object)}
     *                 and {@link #closeFromBackground(Object, Object)})
     * @param reusable whether the new Job instance is reusable
     */
    protected DuoJob(@NonNull final JobWorker worker,
                     @NonNull final C1 context1, @NonNull final C2 context2,
                     final boolean reusable) {
        super(Check.nonNull(worker), reusable);
        contextsHolder = new AtomicReference<>(new Contexts<>(context1, context2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isClosed() {
        return contextsHolder.get() == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void run() {
        final Contexts<C1, C2> contexts;
        final boolean closing;
        if (isReusable()) {
            contexts = contextsHolder.get();
            closing = false;
        } else {
            contexts = contextsHolder.getAndSet(null);
            closing = contexts != null; // if contexts is null then the job has been already closed so it's not "closing"
        }

        if (contexts != null)
            callDoHook(contexts, closing);
    }

    @SuppressLint("WrongThread")
    private void callDoHook(final Contexts<C1, C2> contexts, final boolean closeWhenDone) {
        RuntimeException mainException = null;
        try {
            if (SystemWorker.MAIN.isCurrentThread())
                doFromMain(contexts.first, contexts.second);
            else
                doFromBackground(contexts.first, contexts.second);
        } catch (RuntimeException e) {
            throw mainException = e;
        } catch (InterruptedException e) {
            handleInterruptedException(e);
        } finally {
            if (closeWhenDone) {
                if (mainException != null)
                    try {
                        callCloseHook(contexts);
                    } catch (RuntimeException e) {
                        mainException.addSuppressed(e);
                    }
                else
                    callCloseHook(contexts);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        final Contexts<C1, C2> contexts = contextsHolder.getAndSet(null);
        if (contexts != null)
            callCloseHook(contexts);
    }

    @SuppressLint("WrongThread")
    private void callCloseHook(final Contexts<C1, C2> contexts) {
        if (SystemWorker.MAIN.isCurrentThread())
            closeFromMain(contexts.first, contexts.second);
        else
            closeFromBackground(contexts.first, contexts.second);
    }

    /**
     * A hook method that gets called when this Job is
     * executed by the main thread.
     * <p>
     * If this Job is executed by any other thread then
     * {@link #doFromBackground(Object, Object)} is called instead.
     * </p>
     * <p>
     * Note that default implementation of this method throws an
     * UnsupportedOperationException to reduce the chances of
     * a subclass in implementing {@link #doFromBackground(Object, Object)}
     * where it should have overridden this method
     * </p>
     *
     * @param context1 the first context that was given at construction time
     * @param context2 the second context that was given at construction time
     */
    @MainThread
    protected void doFromMain(@NonNull final C1 context1, @NonNull final C2 context2) {
        methodNotImplemented("doFromMain(context1, context2)");
    }

    /**
     * A hook method that gets called when this Job instance is
     * executed by a background thread.
     * <p>
     * If this Job is executed by the main thread then
     * {@link #doFromMain(Object, Object)} is called instead.
     * </p>
     * <p>
     * Note that default implementation of this method throws an
     * UnsupportedOperationException to reduce the chances of
     * a subclass in implementing {@link #doFromMain(Object, Object)}
     * where it should have overridden this method
     * </p>
     *
     * @param context1 the first context that was given at construction time
     * @param context2 the second context that was given at construction time
     */
    @WorkerThread
    protected void doFromBackground(@NonNull final C1 context1, @NonNull final C2 context2)
            throws InterruptedException {
        methodNotImplemented("doFromBackground(context1, context2)");
    }

    /**
     * A hook method that gets called when this Job is being
     * closed from the main thread.
     *
     * @param context1 the first context that was given at construction time
     * @param context2 the second context that was given at construction time
     */
    @MainThread
    protected void closeFromMain(@NonNull final C1 context1, @NonNull final C2 context2) {
    }

    /**
     * A hook method that gets called when this Job is being
     * closed from a background thread.
     *
     * @param context1 the first context that was given at construction time
     * @param context2 the second context that was given at construction time
     */
    @WorkerThread
    protected void closeFromBackground(@NonNull final C1 context1, @NonNull final C2 context2) {
    }

    @NonNull
    @Override
    public String toString() {
        final Contexts<C1, C2> contexts = contextsHolder.get();
        if (contexts == null)
            return super.toString() + "{context=null}";
        else
            return super.toString() + "{" +
                    "context1=" + contexts.first +
                    ", context2=" + contexts.second +
                    '}';
    }

    private static final class Contexts<C1, C2> {
        final C1 first;
        final C2 second;

        private Contexts(@NonNull final C1 first, @NonNull final C2 second) {
            this.first = Check.nonNull(first);
            this.second = Check.nonNull(second);
        }
    }
}
