package dz.jsoftware95.silverbox.android.common;
import dz.jsoftware95.queue.common.Supplier;

public interface Lazy<T> extends Supplier<T> {

    T get();
}
