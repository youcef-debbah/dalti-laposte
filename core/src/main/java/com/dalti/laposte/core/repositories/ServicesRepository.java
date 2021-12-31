package com.dalti.laposte.core.repositories;

import androidx.annotation.AnyThread;
import androidx.annotation.Nullable;

import com.dalti.laposte.core.entity.Service;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Lazy;
import dz.jsoftware95.silverbox.android.backend.LoadableRepository;

@Singleton
@AnyThread
public class ServicesRepository extends LoadableRepository<Service, LoadedService, ServiceDAO> {

    @Inject
    @AnyThread
    public ServicesRepository(Lazy<ServiceDAO> dao) {
        super(dao);
    }

    @Override
    @Nullable
    protected LoadedService loadFrom(@Nullable Service data) {
        return LoadedService.from(data);
    }
}
