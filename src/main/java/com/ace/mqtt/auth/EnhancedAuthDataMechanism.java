package com.ace.mqtt.auth;

import com.ace.mqtt.crypto.MACCalculator;
import com.ace.mqtt.utils.AuthData;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ace.mqtt.utils.StringUtils.bytesToHex;
import static com.ace.mqtt.utils.StringUtils.hexStringToByteArray;

public class EnhancedAuthDataMechanism extends ACEEnhancedAuthMechanism {
    private final static Logger LOGGER = Logger.getLogger(EnhancedAuthDataMechanism.class.getName());

    @NotNull
    private final TokenRequestResponse token;

    public EnhancedAuthDataMechanism(@NotNull final TokenRequestResponse token) {
        this.token = token;
    }

    @Override
    public @NotNull CompletableFuture<Void> onAuth(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Connect connect,
            @NotNull final Mqtt5EnhancedAuthBuilder authBuilder) {
        final CompletableFuture<Void> future = new CompletableFuture<>();
        final MACCalculator macCalculator = new MACCalculator(
                token.getCnf().getJwk().getK(),
                token.getCnf().getJwk().getAlg());
        final byte[] pop = macCalculator.compute_hmac(token.getAccess_token().getBytes());
        LOGGER.log(Level.FINE, String.format("Calculated POP:\t%s", bytesToHex(pop)));
        final AuthData authData = new AuthData(token.getAccess_token(), pop);
        authBuilder.data(authData.getCompleteAuthData());
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
        LOGGER.log(Level.FINE, String.format("Received CONNACK:\t%s", connAck));
        return CompletableFuture.completedFuture(Boolean.TRUE);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> onReAuthSuccess(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Auth auth) {
        return null;
    }

}
