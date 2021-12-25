package com.dalti.laposte.core.ui.scanner;

public interface ActivationCodeListener {
    boolean onCodeFound(String code);

    default boolean onCodeNotFound() {
        return false;
    }
}
