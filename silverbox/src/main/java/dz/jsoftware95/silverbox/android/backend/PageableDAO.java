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
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.room.RoomDatabase;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dz.jsoftware95.queue.common.Consumer;

@WorkerThread
public abstract class PageableDAO<V extends VisualItem> implements DataLoader<V> {

    public static final int DEFAULT_PAGE_SIZE = 128;
    private static final String[] NO_TABLES = {};

    public abstract RoomDatabase getDatabase();

    @NonNull
    public String[] getTables() {
        return NO_TABLES;
    }

    public abstract int count();

    @Nullable
    public abstract List<V> getRange(final long start, final long count);

    @NonNull
    public final List<V> loadRange(final long start, final long count) {
        final List<V> loadedRange = getRange(start, count);
        return loadedRange != null ? loadedRange : Collections.emptyList();
    }

    public int getPageSize() {
        return DEFAULT_PAGE_SIZE;
    }

    public void forEach(Consumer<V> action) {
        Objects.requireNonNull(action);
        long page = getPageSize();
        long index = 0;
        List<V> items;
        while ((items = loadRange(index, page)).size() > 0) {
            index += page;
            for (V item : items)
                action.accept(item);
        }
    }
}
