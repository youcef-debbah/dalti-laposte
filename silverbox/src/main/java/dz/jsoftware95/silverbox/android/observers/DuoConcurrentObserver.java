package dz.jsoftware95.silverbox.android.observers;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.ForOverride;

/**
 * A ConcurrentObserver that has access to two external contexts.
 * <p>
 * the context objects will be available as a parameter
 * to {@link #onUpdate(Object, Object, Object)} and {@link #onClose(Object, Object)}.
 * The contexts will get nulled when this instance {@linkplain #isClosed() is closed}
 * </p>
 *
 * @param <C1> the type of the first context
 * @param <C2> the type of the second context
 * @param <D>  The type of {@link #onChanged(Object)} parameter
 */
@AnyThread
public abstract class DuoConcurrentObserver<C1, C2, D> implements ConcurrentObserver<D> {

    private final AtomicReference<Contexts<C1, C2>> contextsHolder;

    /**
     * Constructs a new DuoConcurrentObserver instance with the given contexts.
     *
     * @param context1 the first context to be accessed as the first parameter within
     *                 {@link #onUpdate(Object, Object, Object)} and {@link #onClose(Object, Object)}
     * @param context2 the second context to be accessed as the second parameter within
     *                 {@link #onUpdate(Object, Object, Object)} and {@link #onClose(Object, Object)}
     */
    protected DuoConcurrentObserver(@NotNull final C1 context1,
                                    @NotNull final C2 context2) {
        contextsHolder = new AtomicReference<>(new Contexts<>(context1, context2));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onChanged(@Nullable final D data) {
        final Contexts<C1, C2> contexts = contextsHolder.get();
        if (contexts != null)
            onUpdate(contexts.first, contexts.second, data);
    }

    /**
     * Called when the data is changed.
     *
     * @param context1 the first context that was given at construction time
     * @param context2 the second context that was given at construction time
     * @param data     the new data
     */
    protected void onUpdate(@NonNull final C1 context1,
                            @NonNull final C2 context2,
                            @Nullable final D data) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void close() {
        final Contexts<C1, C2> contexts = contextsHolder.getAndSet(null);
        if (contexts != null)
            onClose(contexts.first, contexts.second);
    }

    /**
     * A hook that gets called when this instance is being closed.
     *
     * @param context1 the first context that was given at construction time
     * @param context2 the second context that was given at construction time
     */
    @ForOverride
    protected void onClose(@NonNull final C1 context1,
                           @NonNull final C2 context2) {
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
    @NonNull
    public String toString() {
        final Contexts<C1, C2> contexts = contextsHolder.get();
        if (contexts != null)
            return "ConcurrentObserver{" +
                    "isClosed=false" +
                    "context1=" + contexts.first +
                    "context2=" + contexts.second +
                    '}';
        else
            return "ConcurrentObserver{isClosed=true}";
    }

    /**
     * Throws a CloneNotSupportedException, always
     *
     * @return nothing, as this method would never finish normally
     * @throws CloneNotSupportedException always
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Throws a NotSerializableException, always.
     * <p>
     * Observers cannot be Serialized because they may
     * contains references to non-serializable contexts
     * </p>
     *
     * @param outputStream serialization output stream (not used)
     * @throws NotSerializableException always
     */
    protected final void writeObject(final ObjectOutputStream outputStream) throws NotSerializableException {
        throw new NotSerializableException("Observers cannot be Serialized because it " +
                "may contains references to non-serializable contexts");
    }

    /**
     * Throws a NotSerializableException, always.
     * <p>
     * Observers cannot be Serialized because they may
     * contains references to non-serializable contexts
     * </p>
     *
     * @param inputStream serialization input stream (not used)
     * @throws NotSerializableException always
     */
    protected final void readObject(final ObjectInputStream inputStream) throws NotSerializableException {
        throw new NotSerializableException("Observers cannot be Serialized because they " +
                "may contains references to non-serializable contexts");
    }

    private static final class Contexts<C1, C2> {
        final C1 first;
        final C2 second;

        private Contexts(@NonNull final C1 first, @NonNull final C2 second) {
            this.first = Assert.nonNull(first);
            this.second = Assert.nonNull(second);
        }
    }
}
