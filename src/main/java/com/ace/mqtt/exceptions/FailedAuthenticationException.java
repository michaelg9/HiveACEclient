package com.ace.mqtt.exceptions;

public class FailedAuthenticationException extends Exception{
    public FailedAuthenticationException(final String errorMessage) {
        super(errorMessage);
    }

}
