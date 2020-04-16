package com.ace.mqtt.http;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.dataclasses.ClientRegistrationRequest;
import com.ace.mqtt.utils.dataclasses.ClientRegistrationResponse;
import com.ace.mqtt.utils.dataclasses.TokenRequest;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import org.jetbrains.annotations.NotNull;

public class RestRequestHandler {
    final HttpsClient httpsClient;

    public RestRequestHandler(
            @NotNull final String targetAS,
            @NotNull final String targetPort) {
        httpsClient = new HttpsClient(targetAS, targetPort);
    }

    public @NotNull TokenRequestResponse requestToken(
            @NotNull final byte[] clientSecret, @NotNull final String grantType,
            @NotNull final String scope, @NotNull final String aud)
            throws ASUnreachableException, FailedAuthenticationException {
        final TokenRequest tokenRequest = new TokenRequest(grantType, scope, aud);
        return httpsClient.secureTokenRequest(clientSecret, tokenRequest);
    }

    public @NotNull ClientRegistrationResponse registerClient(
            @NotNull final String clientName, @NotNull final String clientURI)
            throws ASUnreachableException, FailedAuthenticationException {
        final ClientRegistrationRequest request = new ClientRegistrationRequest(clientName, clientURI);
        return httpsClient.registerClient(request);
    }

}
