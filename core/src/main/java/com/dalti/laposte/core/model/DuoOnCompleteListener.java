package com.dalti.laposte.core.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import dz.jsoftware95.silverbox.android.observers.DuoMainObserver;

public abstract class DuoOnCompleteListener<C1, C2, T> extends DuoMainObserver<C1, C2, Task<T>> implements OnCompleteListener<T> {

    public DuoOnCompleteListener(@NonNull C1 context1, @NonNull C2 context2) {
        super(context1, context2);
    }

    public DuoOnCompleteListener(@NonNull Lifecycle lifecycle, @NonNull C1 context1, @NonNull C2 context2) {
        super(lifecycle, context1, context2);
    }

    @Override
    public void onComplete(@NonNull Task<T> task) {
        onChanged(task);
    }
}
