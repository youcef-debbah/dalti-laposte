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

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.paging.PositionalDataSource;

import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;

import dz.jsoftware95.silverbox.android.common.Assert;

@WorkerThread
class PageableDataSource<T extends VisualItem> extends PositionalDataSource<T> {

    @NonNull
    private final PageableDAO<T> dao;

    protected PageableDataSource(@NonNull final PageableDAO<T> dao) {
        Assert.isNotMainThread();
        this.dao = Assert.nonNull(dao);
    }

    @Override
    public void loadInitial(@NonNull final LoadInitialParams params,
                            @NonNull final LoadInitialCallback<T> callback) {
        final int totalCount = dao.count();
        if (totalCount == 0)
            callback.onResult(Collections.emptyList(), 0, 0);
        else {
            final int firstLoadPosition = computeInitialLoadPosition(params, totalCount);
            final int firstLoadSize = computeInitialLoadSize(params, firstLoadPosition, totalCount);

            final List<T> list = dao.loadRange(firstLoadPosition, firstLoadSize);
            if (list.size() == firstLoadSize)
                callback.onResult(list, firstLoadPosition, totalCount);
            else
                invalidate();
        }
    }

    @Override
    public void loadRange(@NonNull final LoadRangeParams params,
                          @NonNull final LoadRangeCallback<T> callback) {
        callback.onResult(dao.loadRange(params.startPosition, params.loadSize));
    }

    @NonNull
    @Override
    public String toString() {
        return "PageableDataSource{" +
                "dao=" + dao +
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
}
