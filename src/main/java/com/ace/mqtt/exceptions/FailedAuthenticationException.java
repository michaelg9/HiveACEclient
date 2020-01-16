package com.ace.mqtt.exceptions;

public class FailedAuthenticationException extends Exception{
    public FailedAuthenticationException(final String errorMessage) {
        this(errorMessage, null);
    }

    public FailedAuthenticationException(final String errorMessage, final Throwable e) {
        super(errorMessage, e);
    }

}
