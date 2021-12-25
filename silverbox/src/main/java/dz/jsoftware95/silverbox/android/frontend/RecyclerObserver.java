package dz.jsoftware95.silverbox.android.frontend;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import dz.jsoftware95.cleaningtools.AutoCleanable;

/**
 *
 */
@MainThread
public abstract class RecyclerObserver extends RecyclerView.AdapterDataObserver implements AutoCleanable {

    public static final int UNKNOWN = -1;

    protected abstract void onUpdate(final int start, final int count);

    @Override
    public final void onChanged() {
        onUpdate(UNKNOWN, UNKNOWN);
    }

    @Override
    public final void onItemRangeChanged(final int positionStart, final int itemCount) {
        onUpdate(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeChanged(final int positionStart, final int itemCount,
                                         @Nullable final Object payload) {
        onUpdate(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeInserted(final int positionStart, final int itemCount) {
        onUpdate(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeRemoved(final int positionStart, final int itemCount) {
        onUpdate(positionStart, itemCount);
    }

    @Override
    public final void onItemRangeMoved(final int fromPosition, final int toPosition, final int itemCount) {
        onUpdate(UNKNOWN, UNKNOWN);
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
