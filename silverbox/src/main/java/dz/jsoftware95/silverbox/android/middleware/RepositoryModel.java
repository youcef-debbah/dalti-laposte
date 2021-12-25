/*
 * Copyright (c) 2018 Youcef DEBBAH (youcef-debbah@hotmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the Software) to deal in the Software without restriction
 * but under the following conditions:
 *
 * - This notice shall be included in all copies and portions of the Software.
 * - The software is provided "AS IS", WITHOUT WARRANTY OF ANY KIND (Implicit or Explicit).
 *
 */

package dz.jsoftware95.silverbox.android.middleware;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.backend.Repository;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.observers.MainObserver;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

@MainThread
public abstract class RepositoryModel<R extends Repository> extends BasicModel {

    private R repository;
    private MainObserver<BackendEvent> backendObserver;

    /**
     * Creates a new Refreshable View Model with an empty refresh observers list
     *
     * @param application the application instance to be used by this model
     */
    public RepositoryModel(@NonNull final Application application,
                           @NonNull final R repository) {
        super(application);
        this.repository = repository;
        repository.addObserver(backendObserver = newStopRefreshObserver(this));
    }


    private static <T extends Repository> MainObserver<BackendEvent> newStopRefreshObserver(RepositoryModel<T> model) {
        return new UnMainObserver<RepositoryModel<T>, BackendEvent>(model) {
            @Override
            protected void onUpdate(@NonNull RepositoryModel<T> model, @Nullable BackendEvent event) {
                if (event != null) {
                    if (event.shouldStartRefreshing() && event.shouldStopRefreshing())
                        model.markAs(!model.isRefreshing());
                    else {
                        if (event.shouldStartRefreshing())
                            model.markAsRefreshing();
                        if (event.shouldStopRefreshing())
                            model.markAsRefreshDone();
                    }
                }
            }

        };
    }

    @NonNull
    protected R getRepository() {
        return Check.nonNull(repository);
    }

    @Override
    protected void onRefreshRequested() {
        repository.invalidate();
    }

    public void addDataObserver(final @NotNull MainObserver<BackendEvent> observer) {
        repository.addObserver(observer);
    }

    public void removeDataObserver(final @NonNull MainObserver<BackendEvent> observer) {
        repository.removeObserver(observer);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        backendObserver = null;
    }
}
