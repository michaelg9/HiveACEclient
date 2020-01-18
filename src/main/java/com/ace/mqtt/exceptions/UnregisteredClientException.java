package com.ace.mqtt.exceptions;

import org.jetbrains.annotations.NotNull;

public class UnregisteredClientException extends RuntimeException {
    public UnregisteredClientException(@NotNull final String message) {
        super(message);
    }
}
