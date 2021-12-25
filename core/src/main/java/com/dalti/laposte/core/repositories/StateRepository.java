package com.dalti.laposte.core.repositories;

import android.content.Context;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.hilt.work.HiltWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedInject;
import dz.jsoftware95.silverbox.android.backend.LazyRepository;
import dz.jsoftware95.silverbox.android.concurrent.Job;
import dz.jsoftware95.silverbox.android.concurrent.UnDatabaseJob;

@Singleton
@AnyThread
public class StateRepository extends LazyRepository<StateDAO> {

    @NonNull
    private final Lazy<ServicesListRepository> servicesListRepository;
    @NonNull
    private final Lazy<DashboardRepository> dashboardRepository;
    @NonNull
    private final Lazy<AbstractActivationRepository> activationRepository;

    @Inject
    @AnyThread
    public StateRepository(@NonNull final Lazy<StateDAO> daoSupplier,
                           @NonNull final Lazy<ServicesListRepository> servicesListRepository,
                           @NonNull Lazy<DashboardRepository> dashboardRepository, @NonNull final Lazy<AbstractActivationRepository> activationRepository) {
        super(daoSupplier);
        this.servicesListRepository = servicesListRepository;
        this.dashboardRepository = dashboardRepository;
        this.activationRepository = activationRepository;
    }

    @AnyThread
    public void invalidateCacheThenFetch() {
        invalidateCacheThenFetch(true);
    }

    public void invalidateCacheOnly() {
        execute(newInvalidateCacheJob(this, null));
    }

    public void invalidateCacheThenFetch(boolean feedback) {
        execute(newInvalidateCacheJob(this, feedback));
    }

    private static Job newInvalidateCacheJob(StateRepository repository, Boolean fetchFeedback) {
        return new UnDatabaseJob<StateRepository>(repository) {
            @Override
            protected void doFromBackground(@NonNull StateRepository repository) {
                repository.requireDAO().invalidateCache();
                if (fetchFeedback != null) {
                    ServicesListRepository servicesListRepository = repository.servicesListRepository.get();
                    if (servicesListRepository.isDataAvailable())
                        servicesListRepository.refresh(fetchFeedback);
                    else {
                        DashboardRepository dashboardRepository = repository.dashboardRepository.get();
                        if (dashboardRepository.isDataAvailable())
                            dashboardRepository.refresh(fetchFeedback);
                    }

                    repository.activationRepository.get().refreshCacheableData(false);
                }
            }
        };
    }

    public void invalidateCacheThenFetchAndWait() throws InterruptedException {
        executeAndWait(newInvalidateCacheJob(this, false));
    }

    @HiltWorker
    public static class InvalidateCacheWorker extends Worker {

        public static final String NAME = "invalidate_cache_worker";
        private final StateRepository stateRepository;

        @AssistedInject
        public InvalidateCacheWorker(@Assisted @NotNull Context context,
                                     @Assisted @NotNull WorkerParameters workerParams,
                                     StateRepository stateRepository) {
            super(context, workerParams);
            this.stateRepository = stateRepository;
        }

        @NonNull
        @Override
        public Result doWork() {
            Teller.logWorkerSession(getInputData());
            try {
                stateRepository.invalidateCacheThenFetchAndWait();
            } catch (InterruptedException e) {
                Teller.logInterruption(e);
            }
            return Result.success();
        }
    }
}
