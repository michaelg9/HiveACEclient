package com.ace.mqtt.utils.dataclasses;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public final class TokenRequest {
    @JsonProperty("grant_type")
    private final String grantType;
    private final String scope;
    private final String aud;
    public TokenRequest(
            @NotNull final String grantType,
            @NotNull final String scope,
            @NotNull final String aud) {
        this.scope = scope;
        this.grantType = grantType;
        this.aud = aud;
    }

    public String getGrantType() {
        return grantType;
    }

    public String getScope() {
        return scope;
    }

    public String getAud() {
        return aud;
    }
}
