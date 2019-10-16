package com.ace.mqtt.http;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.OauthHttpClient;
import com.ace.mqtt.utils.dataclasses.TokenRequest;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import org.jetbrains.annotations.NotNull;

public class RequestHandler {
    private final String targetAS;
    private final String targetPort;

    public RequestHandler(@NotNull final String targetAS, @NotNull final String targetPort) {
        this.targetAS = targetAS;
        this.targetPort = targetPort;
    }

    public @NotNull TokenRequestResponse requestToken(
            @NotNull final String clientID,
            @NotNull final String clientSecret) throws ASUnreachableException, FailedAuthenticationException {
        final OauthHttpClient oauthHttpClient = new OauthHttpClient(this.targetAS, this.targetPort);
        final TokenRequest tokenRequest = new TokenRequest("client_credentials", "sub", "humidity");
        return oauthHttpClient.tokenRequest(clientID + ":" + clientSecret, tokenRequest);
    }

}
