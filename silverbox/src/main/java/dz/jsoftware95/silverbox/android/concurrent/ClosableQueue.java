package dz.jsoftware95.silverbox.android.concurrent;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.queue.common.Consumer;
import dz.jsoftware95.silverbox.android.common.Check;

public class ClosableQueue<E> {

    private final AtomicReference<ConcurrentLinkedQueue<E>> data = new AtomicReference<>(new ConcurrentLinkedQueue<>());

    public void queue(E element) {
        ConcurrentLinkedQueue<E> data = this.data.get();
        Check.nonNull(data);
        data.add(element);
    }

    public boolean queueIfPossible(E element) {
        ConcurrentLinkedQueue<E> data = this.data.get();
        if (data != null)
            return data.add(element);
        else
            return false;
    }

    public boolean isClosed() {
        return data.get() != null;
    }

    public void close(Consumer<E> consumer) {
        ConcurrentLinkedQueue<E> data = this.data.getAndSet(null);
        if (data != null && consumer != null)
            for (E element : data)
                consumer.accept(element);
    }
}
