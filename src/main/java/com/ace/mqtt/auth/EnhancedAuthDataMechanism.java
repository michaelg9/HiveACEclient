package com.ace.mqtt.auth;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EnhancedAuthDataMechanism extends ACEEnhancedAuthMechanism {
    @NotNull private final String clientID;
    @NotNull private final String clientSecret;
    @NotNull private final RequestHandler requestHandler;

    public EnhancedAuthDataMechanism(
            @NotNull final String clientID,
            @NotNull final String clientSecret,
            @NotNull final RequestHandler requestHandler) {
        this.clientID = clientID;
        this.clientSecret = clientSecret;
        this.requestHandler = requestHandler;
    }

    @Override
    public @NotNull CompletableFuture<Void> onAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Connect connect,
            @NotNull final Mqtt5EnhancedAuthBuilder authBuilder) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            final String token;
            try {
                final TokenRequestResponse tokenRequestResponse = requestHandler.requestToken(this.clientID, this.clientSecret);
                token = tokenRequestResponse.access_token + "NEXTdisig";
                authBuilder.data(token.getBytes());
                future.complete(null);
            } catch (final ASUnreachableException | FailedAuthenticationException e) {
                e.printStackTrace();
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Void> onReAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5AuthBuilder authBuilder) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onServerReAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth, @NotNull final Mqtt5AuthBuilder authBuilder) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onContinue(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth, @NotNull final Mqtt5AuthBuilder authBuilder) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.complete(Boolean.TRUE);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onReAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth) {
        return null;
    }

    @Override
    public void onAuthRejected(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {

    }

    @Override
    public void onReAuthRejected(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Disconnect disconnect) {

    }

    @Override
    public void onAuthError(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Throwable cause) {

    }

    @Override
    public void onReAuthError(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Throwable cause) {

    }
}
