package dz.jsoftware95.silverbox.android.concurrent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A serializable object that that is suitable to use as in {@code synchronized} statements.
 * <br>
 * A typical usage of this class would as follow:
 * <pre>{@code
 *     private final Object initMutex = new Mutex();
 *
 *     private void init() {
 *         synchronized (initMutex) {
 *             // some initialization code goes here
 *         }
 *     }
 * }</pre>
 * <p>
 * Using the intrinsic lock of any private object if fine but what makes this class
 * more convenient is the fact that it is Serializable which avoid the need to
 * reinitialize the instance on deserialization, in addition to that all methods
 * of this class are NO-OP and simply throw an Exception immediately to avoid
 * potential misuse.
 * </p>
 * <p>
 * It is recommended to store an instance of this class in
 * a reference of type {@code Object} instead of a {@code Mutex} as
 * this will emphasis on the fact that this class declare no methods
 * except the ones that are inherited from the {@code Object} class
 * </p>
 */
public final class Mutex implements Serializable {

    private static final long serialVersionUID = -2082294696627528075L;

    /**
     * Throws UnsupportedOperationException, always
     *
     * @return nothing as this method would never complete normally
     */
    @NonNull
    @Override
    public String toString() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws UnsupportedOperationException, always
     *
     * @param other the reference object with which to compare (not used)
     * @return nothing as this method would never complete normally
     */
    @Override
    public boolean equals(@Nullable final Object other) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws UnsupportedOperationException, always
     *
     * @return nothing as this method would never complete normally
     */
    @Override
    public int hashCode() {
        throw new UnsupportedOperationException();
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
    }
}
