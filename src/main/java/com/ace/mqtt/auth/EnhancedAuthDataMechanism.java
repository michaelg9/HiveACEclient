package com.ace.mqtt.auth;

import com.ace.mqtt.crypto.AuthCalculator;
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
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class EnhancedAuthDataMechanism extends ACEEnhancedAuthMechanism {

    @NotNull
    private final RequestHandler requestHandler;

    public EnhancedAuthDataMechanism(
            @NotNull final RequestHandler requestHandler) {

        this.requestHandler = requestHandler;
    }

    @Override
    public @NotNull CompletableFuture<Void> onAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Connect connect,
            @NotNull final Mqtt5EnhancedAuthBuilder authBuilder) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                final TokenRequestResponse tokenRequestResponse = requestHandler.requestToken();
                authBuilder.data(
                        new AuthCalculator(
                                tokenRequestResponse.getCnf().getJwk().getK(),
                                tokenRequestResponse.getAccess_token(),
                                tokenRequestResponse.getCnf().getJwk().getAlg())
                                .getCombinedAuthData(connect));
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
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth,
            @NotNull final Mqtt5AuthBuilder authBuilder) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onContinue(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth,
            @NotNull final Mqtt5AuthBuilder authBuilder) {
        return null;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onReAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth) {
        return null;
    }

}
