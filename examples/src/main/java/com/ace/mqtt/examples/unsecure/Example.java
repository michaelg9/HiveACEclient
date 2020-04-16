package com.ace.mqtt.examples.unsecure;

import com.ace.mqtt.builder.ClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.nimbusds.jose.JOSEException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class Example {

    public static void main(final String[] args)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException {
        if (args.length == 0) System.exit(1);
        run(args[0]);
    }

    public static void run(final String configFile)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        final Mqtt3Client client = new ClientBuilder.Ace3ClientBuilder(config).withAuthentication(true).connect();
        final byte[] payload;
        try {
            payload = encryptPayload("This is my message".getBytes(), "thisispass".toCharArray());
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        client.toAsync()
                .publishWith()
                .topic(config.aud)
                .payload(payload)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        System.out.println("Failed publish :( " + throwable);
                    } else if (!client.getState().isConnected()) {
                        System.out.println("Failed publish :(  broker disconnected us. Probably authorization error");
                    } else {
                        System.out.println("Successful publish " + publish);
                    }
                    if (client.getState().isConnected()) {
                        client.toBlocking().disconnect();
                    } else {
                        throw new IllegalStateException("Should have been connected");
                    }
                });
    }

    private static byte[] encryptPayload(final byte[] payload, final char[] password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
            InvalidKeySpecException {
        final SecureRandom sr = SecureRandom.getInstanceStrong();
        final byte[] iv = new byte[128/8];
        sr.nextBytes(iv);
        final IvParameterSpec ivspec = new IvParameterSpec(iv);

        final byte[] salt = new byte[16];
        sr.nextBytes(salt);
        final KeySpec spec = new PBEKeySpec(password, salt, 65536, 256); // AES-256
        final SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final byte[] key = f.generateSecret(spec).getEncoded();
        final SecretKeySpec skey = new SecretKeySpec(key, "AES");

        final Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ci.init(Cipher.ENCRYPT_MODE, skey, ivspec);
        final byte[] result = ci.doFinal(payload);
        System.out.println(Arrays.toString(result));
        return result;
    }
}
