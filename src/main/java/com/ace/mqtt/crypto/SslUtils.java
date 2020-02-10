package com.ace.mqtt.crypto;

import com.ace.mqtt.utils.AuthData;
import com.hivemq.client.mqtt.MqttClientSslConfig;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;

public class SslUtils {

    public static MqttClientSslConfig getSslConfig(
            final String clientKeyFilename, final String clientTrustStoreFilename, final char[] keyStorePass,
            final char[] trustStorePass) {
        //Create key store
        final KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(new File(clientKeyFilename), keyStorePass);
        } catch (final IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        final KeyManagerFactory kmf;
        final TrustManagerFactory tmf;
        try {
            kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keyStorePass);
            //Create trust store
            final KeyStore trustStore = KeyStore.getInstance(new File(clientTrustStoreFilename), trustStorePass);
            tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
        } catch (final IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return MqttClientSslConfig.builder().keyManagerFactory(kmf).trustManagerFactory(tmf).build();
    }

    private static PublicKey getBrokerPK(final String clientTrustStoreFilename, final char[] trustStorePass) {
        final KeyStore trustStore;
        try {
            trustStore = KeyStore.getInstance(new File(clientTrustStoreFilename), trustStorePass);
            if (!trustStore.aliases().hasMoreElements()) throw new KeyStoreException("Didn't find broker's certificate in trust store");
            return trustStore.getCertificate(trustStore.aliases().nextElement()).getPublicKey();
        } catch (final IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptUsingPK(final String clientTrustStoreFilename, final char[] trustStorePass, final byte[] inputData)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {
        final PublicKey key = SslUtils.getBrokerPK(clientTrustStoreFilename, trustStorePass);
        final Cipher cipher = Cipher.getInstance(key.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(inputData);
    }
//
//    public static byte[] encryptData() {
//        /* Encrypt the message. */
//        final Cipher cipher;
//        try {
//            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        } catch (final NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Misconfigured AES encryption", e);
//        }
//        cipher.init(Cipher.ENCRYPT_MODE, secret);
//        final AlgorithmParameters params = cipher.getParameters();
//        try {
//            final byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
//        } catch (final InvalidParameterSpecException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Misconfigured AES encryption", e);
//        }
//        try {
//            return cipher.doFinal("Hello, World!".getBytes(StandardCharsets.UTF_8));
//        } catch (final IllegalBlockSizeException | BadPaddingException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Misconfigured AES encryption", e);
//        }
//    }
//
//    public static void generateAESKey(final char[] pop) throws InvalidKeySpecException {
//        SecretKeyFactory factory = null;
//        try {
//            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
//        } catch (final NoSuchAlgorithmException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Algorithm not supported", e);
//        }
//        final KeySpec spec = new PBEKeySpec(pop, salt, 65536, 128);
//        final SecretKey tmp = factory.generateSecret(spec);
//        final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
//    }
//
//    public static SealedObject seal(final AuthData data, final byte[] key) {
//        final Cipher cipher;
//        try {
//            cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
//        } catch (final NoSuchAlgorithmException | NoSuchPaddingException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Algorithm not supported", e);
//        }
//        try {
//            cipher.init(Cipher.ENCRYPT_MODE, key, );
//        } catch (final InvalidKeyException e) {
//            throw new IllegalStateException("Invalid key for AES", e);
//        }
//        SealedObject sealedObject = null;
//        try {
//            sealedObject = new SealedObject(data, cipher);
//        } catch (final IOException | IllegalBlockSizeException e) {
//            e.printStackTrace();
//            throw new IllegalStateException("Unable to seal object", e);
//        }
//        return sealedObject;
//    }
//
//    public static SecretKey fromStringToAESkey(final String s) {
//        //256bit key need 32 byte
//        byte[] rawKey = new byte[32];
//        // if you don't specify the encoding you might get weird results
//        byte[] keyBytes = new byte[0];
//        try {
//            keyBytes = s.getBytes("ASCII");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        System.arraycopy(keyBytes, 0, rawKey, 0, keyBytes.length);
//        SecretKey key = new SecretKeySpec(rawKey, "AES");
//        return key;
//    }
}
