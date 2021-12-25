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

package dz.jsoftware95.silverbox.android.frontend;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.common.Check;
import dz.jsoftware95.silverbox.android.common.ForOverride;

@MainThread
public abstract class UnRecyclerObserver<C> extends RecyclerObserver {

    @Nullable
    private C context;

    protected UnRecyclerObserver(@NonNull final C context) {
        this.context = Assert.nonNull(context);
    }

    @Override
    protected final void onUpdate(final int start, final int count) {
        onUpdate(Check.nonNull(context), start, count);
    }

    protected abstract void onUpdate(@NonNull C context, final int start, final int count);

    @Override
    public final void close() {
        final C context = this.context;
        if (context != null) {
            this.context = null;
            onClose(context);
        }
    }

    @Override
    public boolean isClosed() {
        return context == null;
    }

    @ForOverride
    protected void onClose(@NonNull final C context) {
    }

    @NonNull
    @Override
    public String toString() {
        return "UnRecyclerObserver{" +
                "context=" + context +
                '}';
    }
}
