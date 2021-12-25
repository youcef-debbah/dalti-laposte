package com.dalti.laposte.core.repositories;

public class CreepyCornerException extends RuntimeException {

    public CreepyCornerException() {
    }

    public CreepyCornerException(String message) {
        super(message);
    }

    public CreepyCornerException(String message, Throwable cause) {
        super(message, cause);
    }

    public CreepyCornerException(Throwable cause) {
        super(cause);
    }
}
