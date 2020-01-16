package com.ace.mqtt.exceptions;

public class ASUnreachableException extends Exception {
    public ASUnreachableException(final String errorMessage) {
        this(errorMessage, null);
    }

    public ASUnreachableException(final String errorMessage, final Throwable e) {
        super(errorMessage, e);
    }
}
