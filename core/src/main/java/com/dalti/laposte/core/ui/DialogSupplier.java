package com.dalti.laposte.core.ui;

import android.os.Bundle;

import dz.jsoftware95.queue.common.Supplier;
import dz.jsoftware95.silverbox.android.common.InputListener;
import dz.jsoftware95.silverbox.android.concurrent.SystemWorker;

public interface DialogSupplier<T extends InputDialog<?, ?>> extends Supplier<T> {

    T get();

    T getExisting();

    static <V, M> void showDialog(DialogSupplier<? extends InputDialog<V, M>> supplier,
                                  InputListener<V, M> listener) {
        if (supplier != null) {
            InputDialog<V, M> inputDialog = supplier.get();
            if (inputDialog != null)
                inputDialog.show(listener);
        }
    }

    static void dismissDialog(DialogSupplier<?> supplier) {
        if (supplier != null) {
            InputDialog<?, ?> cache = supplier.getExisting();
            if (cache != null)
                cache.dismiss();
        }
    }

    static void saveDialog(DialogSupplier<?> supplier, Bundle outState) {
        if (supplier != null) {
            InputDialog<?, ?> cache = supplier.getExisting();
            if (cache != null)
                cache.saveState(outState);
        }
    }

    static <T, M> void showDialog(T value, DialogSupplier<? extends InputDialog<T, M>> dialogSupplier,
                                  InputListener<T, M> listener) {
        if (dialogSupplier != null) {
            Runnable job = () -> {
                InputDialog<T, M> inputDialog = dialogSupplier.get();
                if (inputDialog != null) {
                    inputDialog.setValue(value);
                    inputDialog.show(listener);
                }
            };

            if (SystemWorker.MAIN.isCurrentThread())
                job.run();
            else
                SystemWorker.MAIN.post(job);
        }
    }
}
