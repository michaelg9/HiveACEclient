package com.ace.mqtt.utils;

import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class AuthData implements Serializable {
    @NotNull private String token;
    @Nullable private byte[] pop = null;

    public AuthData(@NotNull final String token, @NotNull final byte[] pop) {
        this(token);
        this.pop = pop;
    }

    public AuthData(@NotNull final String token) {
        this.token = token;
    }

    @NotNull
    public ByteBuffer getTokenAuthData() {
        final MqttUtf8StringImpl token = getTokenEncoded();
        final ByteBuffer results = ByteBuffer.allocate(token.encodedLength());
        results.putShort((short) (token.encodedLength() - 2));
        results.put(token.toByteBuffer());
        results.rewind();
        return results;
    }

    @NotNull
    public ByteBuffer getPOPAuthData() {
        if (pop == null) {
            throw new IllegalStateException("Should set POP before requesting complete auth data");
        }
        final ByteBuffer results = ByteBuffer.allocate(pop.length + 2);
        results.putShort((short) pop.length);
        results.put(pop);
        results.rewind();
        return results;
    }

    @NotNull
    public ByteBuffer getCompleteAuthData() {
        if (pop == null) {
            throw new IllegalStateException("Should set POP before requesting complete auth data");
        }
        final MqttUtf8StringImpl token = getTokenEncoded();
        final ByteBuffer results = ByteBuffer.allocate(token.encodedLength() + 2 + pop.length);
        results.putShort((short) (token.encodedLength() - 2));
        results.put(token.toByteBuffer());
        results.putShort((short) pop.length);
        results.put(pop);
        results.rewind();
        return results;
    }

    @NotNull
    public String getToken() {
        return token;
    }

    public void setToken(@NotNull final String token) {
        this.token = token;
    }

    @NotNull
    public MqttUtf8StringImpl getTokenEncoded() {
        return MqttUtf8StringImpl.of(token);
    }

    @Nullable
    public byte[] getPOP() {
        return pop;
    }

    public void setPop(@NotNull final byte[] pop) {
        this.pop = pop;
    }
}
