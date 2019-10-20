package com.ace.mqtt.crypto;

import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * TODO:
 * compute MAC over whole Connect
 * condition on algorithm
 * define separator
 */
public class AuthCalculator {

    private final String separator = "NEXT";
    private final String algorithm = "HmacSHA256";
    private final String key;
    private final String token;

    public AuthCalculator(final String key, final String token, String algorithm) {
        this.key = key;
        this.token = token;
    }

    public byte[] getAuthData(final Mqtt5Connect connect) {
        final String result = token + separator + compute_hmac(connect);
        return result.getBytes();
    }

    private String compute_hmac(final Mqtt5Connect connect) {
        final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
        try {
            final Mac sha512_HMAC = Mac.getInstance(algorithm);
            final SecretKeySpec keySpec = new SecretKeySpec(byteKey, algorithm);
            sha512_HMAC.init(keySpec);
            final byte[] mac_data = sha512_HMAC.doFinal(token.getBytes());
            return bytesToHex(mac_data);
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            // should never happen for the case of HmacSHA1 / HmacSHA256
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String bytesToHex(final byte[] bytes) {
        final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
        final char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            final int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}