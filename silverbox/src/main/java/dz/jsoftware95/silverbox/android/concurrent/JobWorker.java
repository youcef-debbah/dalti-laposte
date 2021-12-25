package dz.jsoftware95.silverbox.android.concurrent;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * An object that executes submitted {@link Runnable} tasks on a
 * single thread that back this instance.
 * <p>
 * Instances of this type can safely be treated as singletons
 * (stored in final or static fields) since nulling references to them
 * would not terminates the thread that back this instance anyways
 * </p>
 */
@AnyThread
public interface JobWorker extends Executor, Serializable {

    /**
     * Returns whether this Executor is backed by the current thread (tasks
     * submitted to this worker will run on the current thread)
     *
     * @return whether the current thread is backing this instance
     */
    @Contract(pure = true)
    boolean isCurrentThread();

    /**
     * Interrupts this thread.
     * <p>
     * Unless the current thread is interrupting itself, which is
     * always permitted, this method may cause a {@link SecurityException}
     * to be thrown.
     */
    void interrupt();

    /**
     * Returns whether this Executor is backed by the main thread of the
     * Android application (tasks submitted to this worker will run on the main thread)
     *
     * @return whether the main thread is backing this instance
     */
    @Contract(pure = true)
    boolean isBackedByMainThread();

    /**
     * The name of this Executor instance
     *
     * @return the name of this instance
     */
    @NonNull
    @Contract(pure = true)
    String getName();

    /**
     * Runs the given <var>runnable</var> on the backing thread of this worker.
     *
     * @param runnable the runnable task
     */
    @Override
    void execute(@NonNull Runnable runnable);

    /**
     * Causes the given <var>runnable</var> to be queued, to be run on the backing thread of this worker
     * after the specified amount of time elapses.
     * <p>
     * Time spent in 'deep sleep' will add an additional delay to execution.
     * </p>
     *
     * @param runnable    the runnable task
     * @param delayMillis The delay (in milliseconds) until the Runnable
     *                    will be executed.
     */
    void executeDelayed(@NonNull Runnable runnable, long delayMillis);

    /**
     * Runs the given <var>runnable</var> on the backing thread and then to wait until that thread
     * has finished running this job, unless this thread is
     * {@linkplain Thread#interrupt interrupted}.
     * <p>
     * Note it is permissible for the current thread (calling thread)
     * to be the backing thread (executing thread).
     * </p>
     *
     * @param job the runnable job
     * @throws InterruptedException if the current thread is interrupted
     *                              while waiting
     */
    void executeAndWait(@NonNull final Job job) throws InterruptedException;

    default void post(Runnable runnable) {
        if (runnable != null) {
            if (isCurrentThread())
                runnable.run();
            else
                execute(runnable);
        }
    }

    static void runAndWait(@NonNull final Job job) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        new DuoJob<Runnable, CountDownLatch>(job.getWorker(), job, latch) {
            @Override
            protected void doFromMain(@NonNull final Runnable runnable, @NonNull final CountDownLatch latch) {
                runnable.run();
            }

            @Override
            protected void closeFromMain(@NonNull final Runnable runnable, @NonNull final CountDownLatch latch) {
                latch.countDown();
            }
        }.execute();

        latch.await();
    }
}
