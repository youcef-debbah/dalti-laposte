package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import org.jetbrains.annotations.NotNull;

import dagger.Lazy;
import dz.jsoftware95.silverbox.android.common.Cache;
import dz.jsoftware95.silverbox.android.concurrent.DuoDatabaseJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.observers.AddLiveDataSourceJob;
import dz.jsoftware95.silverbox.android.observers.ObserversUtil;

public abstract class LoadableRepository<T, D, DAO extends DataLoader<T>> extends LazyRepository<DAO> {

    protected final Cache<Long, LiveData<D>> cache = new Cache<>(4);

    public LoadableRepository(Lazy<DAO> daoSupplier) {
        super(daoSupplier);
    }

    @MainThread
    public LiveData<D> getLoadedData(long id) {
        return cache.computeIfAbsent(id, this::loadNewData);
    }

    private LiveData<D> loadNewData(long id) {
        MediatorLiveData<D> liveData = new MediatorLiveData<>();
        execute(newAddDataLoaderJob(this, liveData, id));
        return liveData;
    }

    @NotNull
    public static <T, D, DAO extends DataLoader<T>> Job newAddDataLoaderJob(@NotNull LoadableRepository<T, D, DAO> repository,
                                                                            @NotNull MediatorLiveData<D> liveData,
                                                                            long id) {
        return new DuoDatabaseJob<LoadableRepository<T, D, DAO>, MediatorLiveData<D>>(repository, liveData) {
            @Override
            protected void doFromBackground(@NotNull LoadableRepository<T, D, DAO> repository,
                                            @NotNull MediatorLiveData<D> liveData) {
                LiveData<D> source = ObserversUtil.map(repository.requireDAO().load(id), repository::loadFrom);
                new AddLiveDataSourceJob<>(source, liveData).execute();
            }
        };
    }

    @Nullable
    @WorkerThread
    protected abstract D loadFrom(@Nullable T data);
}
