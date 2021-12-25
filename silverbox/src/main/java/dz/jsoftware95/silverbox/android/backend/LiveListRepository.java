package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.AnyThread;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import dagger.Lazy;
import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.concurrent.DuoJob;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;

@AnyThread
public abstract class LiveListRepository<V extends VisualItem, DAO extends PageableDAO<V>>
        extends LazyRepository<DAO> {

    public static final long MIN_AUTO_REFRESH_DELAY = TimeUnit.SECONDS.toMillis(35);

    protected final AtomicLong autoFetchCache = new AtomicLong();
    private volatile RefreshableFactory<V> factory;

    @AnyThread
    public LiveListRepository(Lazy<DAO> daoSupplier) {
        super(daoSupplier);
    }

    @Override
    @CallSuper
    public void ontInitialize() {
        this.factory = new RefreshableFactory<>(requireDAO());
        onAutoRefresh();
    }

    protected void onAutoRefresh() {
    }

    public void autoRefresh() {
        long now = System.currentTimeMillis();
        long lastAutoFetch = autoFetchCache.getAndSet(now);
        if (now - lastAutoFetch > MIN_AUTO_REFRESH_DELAY)
            execute(newAutoRefreshJob(this));
    }

    private static Job newAutoRefreshJob(LiveListRepository<?, ?> servicesListRepository) {
        return new UnDatabaseJob<LiveListRepository<?, ?>>(servicesListRepository) {
            @Override
            protected void doFromBackground(@NotNull LiveListRepository<?, ?> repository) {
                repository.onAutoRefresh();
            }
        };
    }

    @Override
    public void invalidate() {
        invalidateDatabaseSources();
    }

    @AnyThread
    public void invalidateDatabaseSources() {
        RefreshableFactory<V> factory = this.factory;
        if (factory != null)
            factory.invalidateAllSources();

        postPublish(DataEvent.DATA_FETCHED);
    }

    /**
     * non blocking
     */
    @NonNull
    @MainThread
    public LiveData<PagedList<V>> getLiveList(@NonNull final PagedList.Config pagingConfig) {
        Assert.isMainThread();
        final LiveList<V> liveList = new LiveList<>(pagingConfig);
        execute(new DuoJob<LiveList<V>, LiveListRepository<V, DAO>>(liveList, this) {
            @Override
            protected void doFromMain(@NonNull final LiveList<V> liveList,
                                      @NonNull final LiveListRepository<V, DAO> repository) {
                liveList.populate(Objects.requireNonNull(repository.factory));
            }
        });
        return liveList.getLiveData();
    }

    private static final class LiveList<V extends VisualItem> {

        final LiveDataWrapper<PagedList<V>> liveDataWrapper = new LiveDataWrapper<>();
        final PagedList.Config pagingConfig;

        public LiveList(@NonNull final PagedList.Config pagingConfig) {
            this.pagingConfig = pagingConfig;
        }

        public void populate(@NonNull final RefreshableFactory<V> factory) {
            liveDataWrapper.setSource(new LivePagedListBuilder<>(factory, pagingConfig).build());
        }

        public LiveData<PagedList<V>> getLiveData() {
            return liveDataWrapper.getLiveData();
        }
    }
}
