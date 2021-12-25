package com.dalti.laposte.core.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import dz.jsoftware95.silverbox.android.observers.UnMainObserver;

public abstract class UnOnCompleteListener<C, T> extends UnMainObserver<C, Task<T>> implements OnCompleteListener<T> {

    protected UnOnCompleteListener(@NonNull C context) {
        super(context);
    }

    protected UnOnCompleteListener(@NonNull Lifecycle lifecycle, @NonNull C context) {
        super(lifecycle, context);
    }

    @Override
    public void onComplete(@NonNull Task<T> task) {
        onChanged(task);
    }
}
