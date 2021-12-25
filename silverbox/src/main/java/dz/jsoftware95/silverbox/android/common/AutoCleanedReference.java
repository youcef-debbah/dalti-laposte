package dz.jsoftware95.silverbox.android.common;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import dz.jsoftware95.cleaningtools.AutoCleanable;

/**
 * A wrapper of a LifecycleOwner that will null it's reference to the wrapped instance
 * when {@link androidx.lifecycle.Lifecycle.Event#ON_DESTROY} event is published.
 * <p>
 * Note that {@link ViewLifecycleRegistry} may fire the {@code ON_DESTROY}
 * event multiple times and thus <em>must not be used with this class</em>
 * </p>
 *
 * @param <T> the type of the wrapped instance
 */
@MainThread
public final class AutoCleanedReference<T extends LifecycleOwner> implements AutoCleanable {

    private static final String REFERENCE_IS_CLOSED = "this reference is closed";

    @Nullable
    private T reference;

    /**
     * Constructs a new AutoCleanedReference with the given <var>reference</var>
     * as it's wrapped instance.
     *
     * @param reference the wrapped instance
     */
    public AutoCleanedReference(@NonNull final T reference) {
        this.reference = Assert.nonNull(reference);
        reference.getLifecycle().addObserver(new AutoCleaner(this));
    }

    /**
     * Returns the wrapped instance
     *
     * @return the wrapped instance
     */
    @Nullable
    public T find() {
        return reference;
    }

    /**
     * Returns the wrapped instance
     *
     * @return the wrapped instance
     * @throws NullPointerException if the instance is null
     */
    @NonNull
    public T get() {
        return Check.nonNull(reference, REFERENCE_IS_CLOSED);
    }

    /**
     * Sets the reference to the wrapped instance to null.
     * <P>
     * Note that this method will be called aromatically
     * when {@link androidx.lifecycle.Lifecycle.Event#ON_DESTROY} event is published
     * </P>
     */
    @Override
    public void close() {
        reference = null;
    }

    /**
     * Returns whether the {@code close} methods has been called on this object.
     *
     * @return {@code true} if this object has been already closed otherwise {@code false}
     */
    @Override
    public boolean isClosed() {
        return reference == null;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String toString() {
        return "AutoCleanedReference{" +
                "reference=" + reference +
                '}';
    }
}
