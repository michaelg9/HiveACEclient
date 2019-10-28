package com.ace.mqtt.http;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.dataclasses.TokenRequest;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import org.jetbrains.annotations.NotNull;

public class RequestHandler {
    private final String targetAS;
    private final String targetPort;
    private final String clientID;
    private final String clientSecret;

    public RequestHandler(
            @NotNull final String targetAS,
            @NotNull final String targetPort,
            @NotNull final String clientID,
            @NotNull final String clientSecret) {
        this.targetAS = targetAS;
        this.targetPort = targetPort;
        this.clientID = clientID;
        this.clientSecret = clientSecret;
    }

    public @NotNull TokenRequestResponse requestToken() throws ASUnreachableException, FailedAuthenticationException {
        String grantType = "client_credentials";
        String scope = "sub";
        String aud = "humidity";
        final OauthHttpClient oauthHttpClient = new OauthHttpClient(this.targetAS, this.targetPort);
        final TokenRequest tokenRequest = new TokenRequest(grantType, scope, aud);
        return oauthHttpClient.tokenRequest(clientID + ":" + clientSecret, tokenRequest);
    }

}
