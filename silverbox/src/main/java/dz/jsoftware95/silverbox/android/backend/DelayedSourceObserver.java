package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import dz.jsoftware95.queue.common.Consumer;
import dz.jsoftware95.queue.common.GlobalUtil;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

public final class DelayedSourceObserver<D> extends UnMainObserver<Consumer<D>, D> {

    private final int delay;
    private final AtomicReference<DelayedData<D>> delayedData = new AtomicReference<>();

    public DelayedSourceObserver(@NonNull Consumer<D> consumer, int delay) {
        super(consumer);
        this.delay = delay;
    }

    @Override
    @MainThread
    protected void onUpdate(@NonNull final Consumer<D> consumer,
                            @Nullable final D data) {
        long id = GlobalUtil.randomLong();
        delayedData.set(new DelayedData<>(consumer, data, id));
        AppWorker.BACKGROUND.executeDelayed(() -> handleDelayedData(id), delay);
    }

    private void handleDelayedData(long id) {
        DelayedData<D> data = delayedData.get();
        if (data != null && data.id == id) {
            data.consumer.accept(data.value);
            delayedData.set(null);
        }
    }

    @Override
    protected void onClose(@NonNull final Consumer<D> consumer) {
        delayedData.set(null);
    }

    private static final class DelayedData<D> {
        final Consumer<D> consumer;
        final D value;
        final long id;

        DelayedData(Consumer<D> consumer, D value, long id) {
            this.consumer = Objects.requireNonNull(consumer);
            this.value = value;
            this.id = id;
        }
    }
}
