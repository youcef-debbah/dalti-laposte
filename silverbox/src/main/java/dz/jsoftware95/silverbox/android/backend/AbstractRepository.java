package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.LDT;
import dz.jsoftware95.silverbox.android.observers.MainObserver;
import dz.jsoftware95.silverbox.android.observers.MainObserversRegistry;

@AnyThread
public abstract class AbstractRepository implements Repository {

    protected final String TAG = getClass().getSimpleName();
    protected final MainObserversRegistry<BackendEvent> observers = new MainObserversRegistry<>();
    private volatile Boolean refreshing = null;

    /**
     * non blocking
     */
    @Override
    @AnyThread
    public void invalidate() {
        postPublish(DataEvent.DATA_FETCHED);
    }

    @Override
    @MainThread
    public void addObserver(@NonNull final MainObserver<BackendEvent> observer) {
        observers.add(observer);
    }

    @Override
    @MainThread
    public void removeObserver(@NonNull final MainObserver<BackendEvent> observer) {
        observers.remove(observer);
    }

    @MainThread
    protected void publish(@NonNull final BackendEvent event) {
        LDT.i("publishing event: " + event);
        updateInternalState(event);
        observers.publish(Assert.nonNull(event));
    }

    @AnyThread
    protected void postPublish(@NonNull final BackendEvent event) {
        LDT.i("posting event: " + event + " to " + observers.count() + " observer");
        updateInternalState(event);
        observers.postPublish(Assert.nonNull(event));
    }

    private void updateInternalState(BackendEvent event) {
        if (event != null) {
            if (event.shouldStopRefreshing())
                refreshing = false;
            else if (event.shouldStartRefreshing())
                refreshing = true;
        }
    }

    @Override
    public boolean isRefreshing() {
        Boolean refreshing = this.refreshing;
        return refreshing != null && refreshing;
    }

    @Override
    public boolean isRefreshedBefore() {
        return this.refreshing != null;
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
     *
     * @param outputStream serialization output stream (not used)
     * @throws NotSerializableException always
     */
    protected final void writeObject(final ObjectOutputStream outputStream) throws NotSerializableException {
        throw new NotSerializableException();
    }

    /**
     * Throws a NotSerializableException, always.
     *
     * @param inputStream serialization input stream (not used)
     * @throws NotSerializableException always
     */
    protected final void readObject(final ObjectInputStream inputStream) throws NotSerializableException {
        throw new NotSerializableException();
    }
}
