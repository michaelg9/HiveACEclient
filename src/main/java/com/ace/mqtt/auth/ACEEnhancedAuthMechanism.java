package com.ace.mqtt.auth;

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public abstract class ACEEnhancedAuthMechanism implements Mqtt5EnhancedAuthMechanism {

    @Override
    public @NotNull MqttUtf8String getMethod() {
        // authentication method defined by the draft
        return MqttUtf8StringImpl.of("ace");
    }

    @Override
    public int getTimeout() {
        return 30;
    }

    public abstract @NotNull CompletableFuture<Void> onAuth(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Connect connect,
            @NotNull Mqtt5EnhancedAuthBuilder authBuilder);

    public abstract @NotNull CompletableFuture<Void> onReAuth(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5AuthBuilder authBuilder);

    public abstract @NotNull CompletableFuture<Boolean> onServerReAuth(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilder authBuilder);

    public abstract @NotNull CompletableFuture<Boolean> onContinue(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilder authBuilder);

    public abstract @NotNull CompletableFuture<Boolean> onAuthSuccess(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5ConnAck connAck);

    public abstract @NotNull CompletableFuture<Boolean> onReAuthSuccess(
            @NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth);

    @Override
    public void onAuthRejected(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5ConnAck connAck) {}

    @Override
    public void onReAuthRejected(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Mqtt5Disconnect disconnect) {}

    @Override
    public void onAuthError(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Throwable cause) {}

    @Override
    public void onReAuthError(
            @NotNull final Mqtt5ClientConfig clientConfig, @NotNull final Throwable cause) {}
}
