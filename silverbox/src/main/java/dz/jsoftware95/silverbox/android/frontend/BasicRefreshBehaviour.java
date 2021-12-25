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
import androidx.lifecycle.Lifecycle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dz.jsoftware95.silverbox.android.common.Assert;
import dz.jsoftware95.silverbox.android.middleware.FrontendEvent;
import dz.jsoftware95.silverbox.android.middleware.StatefulModel;
import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

@MainThread
public class BasicRefreshBehaviour extends UnMainObserver<SwipeRefreshLayout, FrontendEvent>
        implements SwipeRefreshLayout.OnRefreshListener {

    @Nullable
    protected StatefulModel model;

    public BasicRefreshBehaviour(@NonNull final Lifecycle lifecycle,
                                 @NonNull final SwipeRefreshLayout layout,
                                 @NonNull final StatefulModel model) {
        super(lifecycle, layout);
        this.model = Assert.nonNull(model);
    }

    @Override
    protected void onUpdate(@NonNull final SwipeRefreshLayout layout,
                            @Nullable final FrontendEvent event) {
        if (model != null) {
            boolean modelIsRefreshing = model.isRefreshing();
            if (modelIsRefreshing && !layout.isRefreshing())
                layout.setRefreshing(true);
            else if (!modelIsRefreshing && layout.isRefreshing())
                layout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        if (model != null)
            model.startRefreshing();
    }

    @Override
    protected void onClose(@NonNull final SwipeRefreshLayout context) {
        model = null;
    }
}
