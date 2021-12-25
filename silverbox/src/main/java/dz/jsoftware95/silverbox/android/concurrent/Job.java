package dz.jsoftware95.silverbox.android.concurrent;

import androidx.annotation.AnyThread;

/**
 * A closable runnable that knows how to execute itself on the correct thread.
 */
@AnyThread
public interface Job extends Runnable, AutoCloseable {

    static Job emptyJob() {
        return new AbstractJob(AppWorker.BACKGROUND, false) {
            @Override
            public void run() {
            }
            @Override
            public void close() {
            }
            @Override
            public boolean isClosed() {
                return true;
            }
        };
    }

    /**
     * Runs this job on the default thread.
     */
    void execute();

    /**
     * Returns the worker that will handler this job if {@link #execute()} is called
     * @return the worker that back this job
     */
    JobWorker getWorker();

    /**
     * Causes this Job to be queued, to be run on the default thread
     * after the specified amount of time elapses.
     * <p>
     * Time spent in 'deep sleep' will add an additional delay to execution.
     * </p>
     *
     * @param delayMillis The delay (in milliseconds) until the Runnable
     *                    will be executed.
     */
    void executeDelayed(long delayMillis);

    /**
     * Runs this job on the default thread and then to wait until that thread
     * has finished running this job, unless this thread is
     * {@linkplain Thread#interrupt interrupted}.
     * <p>
     * Note it is permissible for the current thread (calling thread)
     * to be the Job's default thread (executing thread).
     * </p>
     *
     * @throws InterruptedException if the current thread is interrupted
     *                              while waiting
     */
    void executeAndWait() throws InterruptedException;

    /**
     * Returns whether this Job is reusable.
     * <p>
     * if a Job is <em>not</em> reusable, then it will be
     * {@linkplain #close() closed} automatically after running it.
     * </p>
     *
     * @return whether this Job is reusable
     */
    boolean isReusable();

    /**
     * Runs this job (manually) on the calling thread.
     * Note that calling {@link #execute()} is preferred
     * over calling this method when it is possible.
     */
    @Override
    void run();

    /**
     * Closes this Job, typically by nulling references to other object, clearing
     * observers, etc.
     * <p>
     * Implementation of this method should be idempotent. In other words,
     * calling this method more than once should not produce any visible side effects
     * (like throwing an exception).
     * </p>
     * <p>
     * Note that the state transformation to 'closed' is guarantied to be
     * atomic (hence thread-safe)
     * </p>
     */
    @Override
    void close();

    /**
     * Returns whether this Job has been closed.
     *
     * @return whether the {@code close} methods has been called on this object.
     */
    boolean isClosed();
}
