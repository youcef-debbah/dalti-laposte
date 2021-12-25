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

package dz.jsoftware95.silverbox.android.backend;

import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.paging.DataSource;
import androidx.room.InvalidationTracker;

import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import dz.jsoftware95.cleaningtools.ConcurrentCleanable;
import dz.jsoftware95.silverbox.android.common.Assert;

@WorkerThread
public class RefreshableFactory<T extends VisualItem> extends DataSource.Factory<Integer, T>
        implements ConcurrentCleanable {

    private final PageableDAO<T> dao;
    private final InvalidationObserver observer;
    private final Collection<DataSource<Integer, T>> dataSources = new CopyOnWriteArrayList<>();

    public RefreshableFactory(@NonNull final PageableDAO<T> pageableDAO) {
        Assert.isNotMainThread();
        dao = Assert.nonNull(pageableDAO);
        observer = new InvalidationObserver(this, pageableDAO.getTables());
        pageableDAO.getDatabase().getInvalidationTracker().addObserver(observer);
    }

    @NotNull
    @Override
    public DataSource<Integer, T> create() {
        Assert.not(isClosed());
        final DataSource<Integer, T> newSource = new PageableDataSource<>(dao);
        dataSources.add(newSource);
        return newSource;
    }

    @AnyThread
    public void invalidateAllSources() {
        final Collection<DataSource<Integer, T>> dataSources = this.dataSources;
        for (final DataSource<Integer, T> source : dataSources) {
            dataSources.remove(source);
            source.invalidate();
        }

        dao.getDatabase().getInvalidationTracker().refreshVersionsAsync();
    }

    @Override
    @AnyThread
    public void close() {
        dataSources.clear();
        observer.close();
    }

    @Override
    @AnyThread
    public boolean isClosed() {
        return observer.isClosed();
    }

    @NonNull
    @Override
    public String toString() {
        return "RefreshableFactory{" +
                "dao: " + dao +
                ", data sources count: " + dataSources.size() +
                '}';
    }

    /**
     * Throws a CloneNotSupportedException, always
     *
     * @return nothing, as this method would never finish normally
     * @throws CloneNotSupportedException always
     */
    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Throws a NotSerializableException, always.
     *
     * @param outputStream serialization output stream (not used)
     * @throws NotSerializableException always
     */
    protected final void writeObject(final ObjectOutputStream outputStream) throws NotSerializableException {
        throw new NotSerializableException();
    }

    /**
     * Throws a NotSerializableException, always.
     *
     * @param inputStream serialization input stream (not used)
     * @throws NotSerializableException always
     */
    protected final void readObject(final ObjectInputStream inputStream) throws NotSerializableException {
        throw new NotSerializableException();
    }

    @AnyThread
    private static class InvalidationObserver extends InvalidationTracker.Observer
            implements ConcurrentCleanable {

        @Nullable
        private volatile RefreshableFactory<?> factory;

        public InvalidationObserver(@NonNull final RefreshableFactory<?> factory,
                                    @NonNull final String[] tables) {
            super(tables);
            this.factory = Assert.nonNull(factory);
        }

        @Override
        public void onInvalidated(@NonNull final Set<String> tables) {
            final RefreshableFactory<?> factory = this.factory;
            if (factory != null)
                factory.invalidateAllSources();
        }

        @Override
        public void close() {
            factory = null;
        }

        @Override
        public boolean isClosed() {
            return factory == null;
        }
    }
}
