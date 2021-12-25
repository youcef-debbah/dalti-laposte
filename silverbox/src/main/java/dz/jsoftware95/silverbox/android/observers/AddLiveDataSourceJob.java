package dz.jsoftware95.silverbox.android.observers;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.silverbox.android.concurrent.DuoJob;

public class AddLiveDataSourceJob<T> extends DuoJob<LiveData<T>, MediatorLiveData<T>> {

    public AddLiveDataSourceJob(LiveData<T> source, MediatorLiveData<T> mediator) {
        super(source, mediator);
    }

    @Override
    protected void doFromMain(@NonNull LiveData<T> source, @NonNull MediatorLiveData<T> mediator) {
        mediator.addSource(source, newDataUpdater(mediator));
    }

    @NotNull
    protected LiveDataUpdater<T> newDataUpdater(@NotNull MediatorLiveData<T> mediator) {
        return new LiveDataUpdater<>(mediator);
    }
}
