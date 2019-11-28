package com.ace.mqtt.auth;

import com.ace.mqtt.crypto.AuthCalculator;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthReasonCode;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class EnhancedAuthDataMechanismWithAuth extends ACEEnhancedAuthMechanism {
    @NotNull
    private final TokenRequestResponse requestToken;
    @Nullable
    private AuthCalculator authCalculator;

    public EnhancedAuthDataMechanismWithAuth(@NotNull final TokenRequestResponse requestToken) {
        this.requestToken = requestToken;
    }

    @Override
    public @NotNull CompletableFuture<Void> onAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Connect connect,
            @NotNull final Mqtt5EnhancedAuthBuilder authBuilder) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        this.authCalculator = new AuthCalculator(
                requestToken.getCnf().getJwk().getK(),
                requestToken.getAccess_token(),
                requestToken.getCnf().getJwk().getAlg());
        authBuilder.data(authCalculator.getSimpleAuthData());
        future.complete(null);
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
        // no need to check reason code, this method is triggered on CONTINUE_AUTHENTICATION by default
        final CompletableFuture<Boolean> future = new CompletableFuture<>();
        if (auth.getData().isEmpty()) {
            future.completeExceptionally(new FailedAuthenticationException("Expected nonce"));
            return future;
        }
        final ByteBuffer authData = auth.getData().get();
        final short nonceLength = authData.getShort();
        if (authData.remaining() < nonceLength) {
            throw new IllegalArgumentException();
        }
        final byte[] nonce = new byte[nonceLength];
        authData.get(nonce);
        final ByteBuffer mac = authCalculator.signNonce(nonce);
        authBuilder.data(mac);
        future.complete(Boolean.TRUE);
        return future;
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
