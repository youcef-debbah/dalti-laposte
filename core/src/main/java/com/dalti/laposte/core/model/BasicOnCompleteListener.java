package com.dalti.laposte.core.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import dz.jsoftware95.silverbox.android.observers.BasicMainObserver;

public abstract class BasicOnCompleteListener<T> extends BasicMainObserver<Task<T>> implements OnCompleteListener<T> {

    public BasicOnCompleteListener() {
    }

    public BasicOnCompleteListener(@NonNull Lifecycle lifecycle) {
        super(lifecycle);
    }

    @Override
    public void onComplete(@NonNull Task<T> task) {
        onChanged(task);
    }
}
