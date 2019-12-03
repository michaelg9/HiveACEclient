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
 * JWK lib?
 */
public class MACCalculator {

    private final String algorithm;
    private final byte[] key;

    public MACCalculator(final byte[] key, final String algorithm) {
        if (algorithm.startsWith("HS")) this.algorithm = algorithm.replace("HS", "HmacSHA");
        else this.algorithm = algorithm;
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
            final Mac mac = Mac.getInstance(algorithm);
            final SecretKeySpec keySpec = new SecretKeySpec(key, algorithm);
            mac.init(keySpec);
            return mac.doFinal(nonce);
        } catch (final NoSuchAlgorithmException | InvalidKeyException e) {
            // should never happen for the case of HmacSHA1 / HmacSHA256
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }



}
