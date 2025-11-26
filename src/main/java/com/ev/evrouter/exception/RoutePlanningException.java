package com.ev.evrouter.exception;

public class RoutePlanningException extends RuntimeException {
    public RoutePlanningException(String message) {
        super(message);
    }

    public RoutePlanningException(String message, Throwable cause) {
        super(message, cause);
    }
}
