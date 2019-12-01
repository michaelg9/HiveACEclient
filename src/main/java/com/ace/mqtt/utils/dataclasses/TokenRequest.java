package com.ace.mqtt.utils.dataclasses;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class TokenRequest {
    private final String grant_type;
    private final String scope;
    private final String aud;
    public TokenRequest(
            @NotNull final String grant_type,
            @NotNull final String scope,
            @NotNull final String aud) {
        this.scope = scope;
        this.grant_type = grant_type;
        this.aud = aud;
    }

    public String getGrant_type() {
        return grant_type;
    }

    public String getScope() {
        return scope;
    }

    public String getAud() {
        return aud;
    }
}
