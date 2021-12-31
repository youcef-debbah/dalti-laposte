package com.dalti.laposte.core.repositories;

import androidx.annotation.NonNull;

import com.dalti.laposte.core.entity.Progress;
import com.dalti.laposte.core.entity.Service;

public interface AdminAction {
    void apply(Progress progress);

    void apply(Service service);

    static void validate(@NonNull Progress progress) {
        validateCurrent(progress);
        validateWaiting(progress);
    }

    static void validateCurrent(@NonNull Progress progress) {
        Integer current = progress.getCurrentToken();
        if (current == null || current < 0)
            progress.setCurrentToken(0);
    }

    static void validateWaiting(@NonNull Progress progress) {
        Integer waiting = progress.getWaiting();
        if (waiting == null || waiting < 0)
            progress.setWaiting(0);
    }
}
