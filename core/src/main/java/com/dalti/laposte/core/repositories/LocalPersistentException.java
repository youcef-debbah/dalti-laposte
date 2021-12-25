package com.dalti.laposte.core.repositories;

public class LocalPersistentException extends RuntimeException {
    public LocalPersistentException() {
        super();
    }

    public LocalPersistentException(String message) {
        super(message);
    }

    public LocalPersistentException(String message, Throwable cause) {
        super(message, cause);
    }

    public LocalPersistentException(Throwable cause) {
        super(cause);
    }
}
