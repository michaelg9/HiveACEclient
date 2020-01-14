package com.ace.mqtt.crypto;

import com.hivemq.client.mqtt.MqttClientSslConfig;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

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
}
