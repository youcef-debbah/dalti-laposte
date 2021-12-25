package com.dalti.laposte.core.repositories;

import androidx.annotation.StringRes;

import com.dalti.laposte.R;

public enum Validity {
    VALID(R.string.input_valid),
    NULL(R.string.input_required),
    TOO_LONG(R.string.input_too_long),
    TOO_SHORT(R.string.input_too_short),
    INVALID(R.string.input_invalid),
    ;

    @StringRes
    private final int msg;

    Validity(int msg) {
        this.msg = msg;
    }

    public int getMessage() {
        return msg;
    }
}
