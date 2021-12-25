package dz.jsoftware95.silverbox.android.concurrent;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import dz.jsoftware95.silverbox.android.common.Check;

/**
 * An enum of JobWorker implementations such that each instance is backed by
 * a thread are started by the Android API.
 */
@AnyThread
public enum SystemWorker implements JobWorker {
    /**
     * A JobWorker that is backed by the Main thread of the Android application
     * (which mean that tasks submitted to this worker will be post to the main thread).
     */
    MAIN(Looper::getMainLooper);

    private static final String WORKER_NAME_SUFFIX = "-SystemWorker";

    /**
     * @serial A Supplier that returns a {@link Looper} that is used to
     * initialize this object, specifically this supplier gets called
     * on construction and deserialization
     */
    private final LooperSupplier supplier;

    @NonNull
    private transient volatile Looper looper;

    @NonNull
    private transient volatile Handler handler;

    SystemWorker(@NotNull final LooperSupplier supplier) {
        final Looper looper = Check.nonNull(supplier.getLooper());
        this.looper = looper;
        this.handler = new Handler(looper);
        this.supplier = supplier;
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

        final Looper looper = Check.nonNull(supplier.getLooper());
        this.looper = looper;
        this.handler = new Handler(looper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public boolean isCurrentThread() {
        return looper.equals(Looper.myLooper());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void interrupt() {
        looper.getThread().interrupt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBackedByMainThread() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    @Contract(pure = true)
    public String getName() {
        return name() + WORKER_NAME_SUFFIX;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(@NonNull final Runnable runnable) {
        handler.post(Objects.requireNonNull(runnable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeDelayed(@NonNull final Runnable runnable, final long delayMillis) {
        if (delayMillis > 0)
            handler.postDelayed(Objects.requireNonNull(runnable), delayMillis);
        else
            execute(runnable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeAndWait(@NonNull final Job job) throws InterruptedException {
        JobWorker.runAndWait(job);
    }

    private interface LooperSupplier extends Serializable {
        Looper getLooper();
    }
}
