package dz.jsoftware95.silverbox.android.observers;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import dz.jsoftware95.queue.common.BiFunction;
import dz.jsoftware95.silverbox.android.backend.DelayedSourceObserver;
import dz.jsoftware95.silverbox.android.concurrent.AppWorker;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;

public class ObserversUtil {

    private ObserversUtil() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    @NonNull
    @AnyThread
    public static <X, Y> LiveData<Y> map(@NonNull final LiveData<X> source,
                                         @NonNull final Function<X, Y> mapFunction) {
        final MediatorLiveData<Y> result = new MediatorLiveData<>();
        SystemWorker.MAIN.post(() -> result.addSource(source, newBackgroundMapper(result, mapFunction)));
        return result;
    }

    @NonNull
    @MainThread
    public static <X, Y> LiveData<Y> mapDelayed(@NonNull final LiveData<X> source, int delay,
                                                @NonNull final Function<X, Y> mapFunction) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(mapFunction);
        final MediatorLiveData<Y> result = new MediatorLiveData<>();
        result.addSource(source, new DelayedSourceObserver<>(x -> result.postValue(mapFunction.apply(x)), delay));
        return result;
    }

    @NotNull
    public static <X, Y> Observer<X> newBackgroundMapper(MutableLiveData<Y> result, @NotNull Function<X, Y> mapFunction) {
        return x -> new DuoJob<Function<X, Y>, MutableLiveData<Y>>(AppWorker.BACKGROUND, mapFunction, result) {
            @Override
            protected void doFromBackground(@NotNull Function<X, Y> mapFunction, @NotNull MutableLiveData<Y> result) {
                result.postValue(mapFunction.apply(x));
            }
        }.execute();
    }

    public static <X1, X2, Y> LiveData<Y> merge(
            @NonNull LiveData<X1> source1,
            @NonNull LiveData<X2> source2,
            @NonNull final BiFunction<X1, X2, Y> mapFunction) {
        final MediatorLiveData<Y> result = new MediatorLiveData<>();
        SystemWorker.MAIN.post(() -> {
            result.addSource(source1, newBackgroundMapper(result, x1 -> mapFunction.apply(x1, source2.getValue())));
            result.addSource(source2, newBackgroundMapper(result, x2 -> mapFunction.apply(source1.getValue(), x2)));
        });
        return result;
    }
}
