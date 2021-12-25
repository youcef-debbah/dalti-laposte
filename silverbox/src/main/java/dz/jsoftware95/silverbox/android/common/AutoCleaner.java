package dz.jsoftware95.silverbox.android.common;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.lang.ref.WeakReference;

import dz.jsoftware95.cleaningtools.AutoCleanable;

/**
 * A {@link androidx.lifecycle.LifecycleObserver} that close (and null's out)
 * the wrapped {@link AutoCleanable} instance when
 * {@link androidx.lifecycle.Lifecycle.Event#ON_DESTROY} event is published
 * by the wrapped instance.
 */
@MainThread
public final class AutoCleaner implements DefaultLifecycleObserver {

    @Nullable
    private WeakReference<AutoCleanable> reference;

    /**
     * Constructs a new AutoCleaner instance with the given <var>cleanable</var>
     * as the wrapped instance.
     *
     * @param cleanable the wrapped instance to be closed on
     *                  {@link androidx.lifecycle.Lifecycle.Event#ON_DESTROY} event
     */
    public AutoCleaner(@Nullable final AutoCleanable cleanable) {
        Assert.isMainThread();
        this.reference = new WeakReference<>(Check.nonNull(cleanable));
    }

    /**
     * Closes this instance and null the reference to the wrapped instance.
     *
     * @param owner the wrapped instance
     */
    @Override
    public void onDestroy(@NonNull final LifecycleOwner owner) {
        owner.getLifecycle().removeObserver(this);
        final WeakReference<AutoCleanable> reference = this.reference;
        if (reference != null) {
            final AutoCleanable cleanable = reference.get();
            this.reference = null;

            if (cleanable != null)
                cleanable.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String toString() {
        final WeakReference<AutoCleanable> reference = this.reference;
        if (reference != null)
            return "AutoCleaner{" +
                    "reference=" + reference.get() +
                    '}';
        else
            return "AutoCleaner{reference=null}";

    }
}
