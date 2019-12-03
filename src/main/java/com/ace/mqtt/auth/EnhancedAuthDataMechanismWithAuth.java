package com.ace.mqtt.auth;

import com.ace.mqtt.crypto.MACCalculator;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.AuthData;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ace.mqtt.utils.StringUtils.bytesToHex;

public class EnhancedAuthDataMechanismWithAuth extends ACEEnhancedAuthMechanism {
    private final static Logger LOGGER = Logger.getLogger(EnhancedAuthDataMechanismWithAuth.class.getName());
    @NotNull private final TokenRequestResponse requestToken;
    @NotNull private final AuthData authData;

    public EnhancedAuthDataMechanismWithAuth(@NotNull final TokenRequestResponse requestToken) {
        this.requestToken = requestToken;
        this.authData = new AuthData(requestToken.getAccess_token());
    }

    @Override
    public @NotNull CompletableFuture<Void> onAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Connect connect,
            @NotNull final Mqtt5EnhancedAuthBuilder authBuilder) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final AuthData authData = new AuthData(requestToken.getAccess_token());
        authBuilder.data(authData.getTokenAuthData());
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
        LOGGER.log(Level.FINE, String.format("Broker AUTH:\t%s\nData:\t%s", auth.toString(), bytesToHex(auth.getData().get())));
        //todo: key encoding?
        final MACCalculator macCalculator = new MACCalculator(
                requestToken.getCnf().getJwk().getK(), requestToken.getCnf().getJwk().getAlg());
        final ByteBuffer nonce = auth.getData().get();
        final byte[] mac = macCalculator.signNonce(nonce);
        LOGGER.log(Level.FINE, String.format("Calculated POP:\t%s", bytesToHex(mac)));
        authData.setPop(mac);
        authBuilder.data(authData.getPOPAuthData());
        future.complete(Boolean.TRUE);
        return future;
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {
        LOGGER.log(Level.FINE, String.format("Received CONNACK:\t%s", connAck));
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onReAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth) {
        return null;
    }
}
