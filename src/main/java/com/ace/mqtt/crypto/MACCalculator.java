package com.ace.mqtt.crypto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * TODO:
 * compute MAC over whole Connect ?
 * condition on algorithm
 */
public class MACCalculator {

    private final String algorithm = "HmacSHA256";
    private final byte[] key;

    public MACCalculator(final byte[] key, final String algorithm) {
        this.key = key;
    }

    public byte[] signNonce(final ByteBuffer lengthAppendedNonce) {
        final short nonceLength = lengthAppendedNonce.getShort();
        if (lengthAppendedNonce.remaining() < nonceLength) {
            throw new IllegalArgumentException();
        }
        final byte[] nonce = new byte[nonceLength];
        lengthAppendedNonce.get(nonce);
        return compute_hmac(nonce);
    }

    public byte[] compute_hmac(final byte[] nonce) {
        try {
            final Mac sha512_HMAC = Mac.getInstance(algorithm);
            final SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            sha512_HMAC.init(keySpec);
            return sha512_HMAC.doFinal(nonce);
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            // should never happen for the case of HmacSHA1 / HmacSHA256
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
