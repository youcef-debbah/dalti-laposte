package dz.jsoftware95.silverbox.android.concurrent;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dz.jsoftware95.silverbox.android.common.Check;

/**
 * A Skeleton implementation of a Job that is backed by a {@link JobWorker} instance.
 */
@AnyThread
public abstract class AbstractJob implements Job {

    @NotNull
    private final JobWorker worker;

    private final boolean reusable;

    /**
     * Creates a new Job instance backed by the given <var>worker</var>.
     *
     * @param worker   an Executor that encapsulates the default thread of this job
     * @param reusable whether this job is reusable,
     *                 If a Job is <em>not</em> reusable, then it will be
     *                 {@linkplain #close() closed} automatically after running it
     */
    protected AbstractJob(@NotNull final JobWorker worker, final boolean reusable) {
        this.worker = Check.nonNull(worker);
        this.reusable = reusable;
    }

    @Override
    public void run() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void execute() {
        worker.execute(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public JobWorker getWorker() {
        return worker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeDelayed(final long delayMillis) {
        worker.executeDelayed(this, delayMillis);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void executeAndWait() throws InterruptedException {
        worker.executeAndWait(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isReusable() {
        return reusable;
    }

    protected void handleInterruptedException(InterruptedException e) {
        Thread.currentThread().interrupt();
        e.printStackTrace();
    }

    /**
     * Throws a runtime exception with a message that indicates that
     * a method with the given <var>methodName</var> is not implemented for this instance.
     *
     * @param methodName the name of the method that is not yet implemented
     * @throws UnsupportedOperationException always
     */
    protected final void methodNotImplemented(@NonNull final String methodName) {
        Check.nonNull(methodName);
        throw new UnsupportedOperationException("method: " + methodName +
                " is not implemented for: " + toString());
    }

    /**
     * Throws a CloneNotSupportedException, always
     *
     * @return nothing, as this method would never finish normally
     * @throws CloneNotSupportedException always
     */
    @Override
    @NotNull
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("You should not clone a job, have some originality!");
    }

    /**
     * Throws a NotSerializableException, always.
     * <p>
     * Jobs can not be Serialized because they may
     * contains references to non-serializable contexts
     * </p>
     *
     * @param outputStream serialization output stream (not used)
     * @throws NotSerializableException always
     */
    protected final void writeObject(final ObjectOutputStream outputStream) throws NotSerializableException {
        throw new NotSerializableException("Jobs cannot be Serialized because they " +
                "may contains references to non-serializable contexts");
    }

    /**
     * Throws a NotSerializableException, always.
     * <p>
     * Jobs can not be Serialized because they may
     * contains references to non-serializable contexts
     * </p>
     *
     * @param inputStream serialization input stream (not used)
     * @throws NotSerializableException always
     */
    protected final void readObject(final ObjectInputStream inputStream) throws NotSerializableException {
        throw new NotSerializableException("Jobs can not be Serialized because they " +
                "may contains references to non-serializable contexts");
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String toString() {
        return "Job(" +
                "worker: " + worker +
                ", is reusable: " + reusable +
                ')';
    }

    @Override
    protected void finalize() {
        close();
    }
}
