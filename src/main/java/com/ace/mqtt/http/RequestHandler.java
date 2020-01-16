package com.ace.mqtt.http;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.dataclasses.TokenRequest;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import org.jetbrains.annotations.NotNull;

public class RequestHandler {
    private final String targetAS;
    private final String targetPort;
    private final byte[] authorizationHeader;

    public RequestHandler(
            @NotNull final String targetAS,
            @NotNull final String targetPort,
            @NotNull final byte[] authorizationHeader) {
        this.targetAS = targetAS;
        this.targetPort = targetPort;
        this.authorizationHeader = authorizationHeader;
    }

    public @NotNull TokenRequestResponse requestToken(final String grantType, final String scope, final String aud)
            throws ASUnreachableException, FailedAuthenticationException {
        final OauthHttpsClient oauthHttpsClient = new OauthHttpsClient(this.targetAS, this.targetPort);
        final TokenRequest tokenRequest = new TokenRequest(grantType, scope, aud);
        return oauthHttpsClient.secureTokenRequest(authorizationHeader, tokenRequest);
    }

}
