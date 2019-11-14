package com.ace.mqtt.http;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.dataclasses.TokenRequest;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

class OauthHttpClient {
    @NotNull
    private final EndpointRetriever endpointRetriever;
    private final ObjectMapper objectMapper = new ObjectMapper();
    final private java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
            .version(java.net.http.HttpClient.Version.HTTP_1_1)
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .build();

    public OauthHttpClient(final String OauthServerAddress, final String OauthServerPort) {
        endpointRetriever = new EndpointRetriever("http", OauthServerAddress, OauthServerPort);
    }


    @NotNull public TokenRequestResponse tokenRequest(
            @NotNull final byte[] authorizationHeader,
            @NotNull final TokenRequest tokenRequest
            ) throws ASUnreachableException, FailedAuthenticationException {
        final String encodedAuth = Base64.getEncoder().encodeToString(authorizationHeader);
        final String stringifiedBody;
        try {
            stringifiedBody = new ObjectMapper().writeValueAsString(tokenRequest);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointRetriever.getEndpoint(EndpointRetriever.ASEndpoint.TOKEN_REQUEST)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(stringifiedBody))
                .setHeader("Authorization", "Basic " + encodedAuth)
                .build();
        final HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            // unable to contact AS server
            throw new ASUnreachableException("Unable to contact AS server");
        }
        if (response.statusCode() != 200) {
            // token request failed, invalid token
            throw new FailedAuthenticationException(response.body());
        }
        final TokenRequestResponse tokenRequestResponse;
        try {
            tokenRequestResponse = objectMapper.readValue(response.body(), TokenRequestResponse.class);
        } catch (final JsonProcessingException e) {
            // Should never happen
            throw new IllegalArgumentException("Failed to parse POST response", e);
        }
        return tokenRequestResponse;
    }
}
