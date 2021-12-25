package com.dalti.laposte.core.ui;

import android.widget.TextView;

public interface Form {

    void submit();

    default boolean submitOnNoError(TextView input) {
        boolean keepKeyboardShown = input != null && input.getError() != null;
        if (!keepKeyboardShown)
            submit();
        return keepKeyboardShown;
    }
}
