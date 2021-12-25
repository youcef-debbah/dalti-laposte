package com.dalti.laposte.core.model;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;

import dz.jsoftware95.silverbox.android.backend.BackendEvent;
import dz.jsoftware95.silverbox.android.frontend.StatefulRecyclerView;
import dz.jsoftware95.silverbox.android.middleware.StatefulModel;
import dz.jsoftware95.silverbox.android.observers.DuoMainObserver;
import dz.jsoftware95.silverbox.android.observers.MainObserver;

public class QueueRecyclerView extends StatefulRecyclerView {

    private MainObserver<BackendEvent> backendObserver;

    public QueueRecyclerView(@NonNull Context context) {
        super(context);
    }

    public QueueRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public QueueRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @NonNull
    public MainObserver<BackendEvent> newBackendObserver(@NonNull final StatefulModel model, Lifecycle lifecycle) {
        return backendObserver = newBackendObserver(lifecycle, this, model);
    }

    @Override
    public void close() {
        super.close();
        if (backendObserver != null) {
            backendObserver.close();
            backendObserver = null;
        }
    }

    private MainObserver<BackendEvent> newBackendObserver(Lifecycle lifecycle,
                                                          StatefulRecyclerView recyclerView,
                                                          StatefulModel model) {
        return new DuoMainObserver<StatefulRecyclerView, StatefulModel, BackendEvent>(lifecycle, recyclerView, model) {
            @Override
            protected void onUpdate(@NonNull final StatefulRecyclerView recycler,
                                    @NonNull final StatefulModel model,
                                    @Nullable final BackendEvent event) {
                if (event != null && event.shouldRestState()) {
                    recycler.clearAdapterData();
                    model.startRefreshing();
                }
            }
        };
    }
}
