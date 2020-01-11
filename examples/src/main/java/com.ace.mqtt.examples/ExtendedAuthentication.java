package com.ace.mqtt.examples;

import com.ace.mqtt.auth.ACEEnhancedAuthMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanismWithAuth;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import static com.ace.mqtt.examples.DiscoverAS.readConfig;

public class ExtendedAuthentication {

    public static MqttClientSslConfig getSslConfig(
            final String clientKeyFilename, final String clientTrustStoreFilename, final char[] key) {
        //Create key store
        final KeyStore keyStore;
        try (final InputStream inKey = ExtendedAuthentication.class.getResourceAsStream(clientKeyFilename)) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inKey, key);
        } catch (final IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        final KeyManagerFactory kmf;
        final TrustManagerFactory tmf;
        try (final InputStream in = ExtendedAuthentication.class.getResourceAsStream(clientTrustStoreFilename)) {
            kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, key);
            //Create trust store
            final KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(in, key);
            tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
        } catch (final IOException | KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return MqttClientSslConfig.builder().keyManagerFactory(kmf).trustManagerFactory(tmf).build();
    }

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, IOException {
        final Properties config = readConfig();
        final String rsServerIP = config.getProperty("RSServerIP");
        final int rsServerPort = Integer.parseInt(config.getProperty("RSServerPort"));
        final String asServerIP = config.getProperty("ASServerIP");
        final String asServerPort = config.getProperty("ASServerPort");
        final String clientID = config.getProperty("ClientID");
        final String clientSecret = config.getProperty("ClientSecret");
        final char[] tlsKeyPassword = config.getProperty("TLSKeyPassword").toCharArray();
        final String clientKeyFilename = config.getProperty("PrivateKeyStoreFilepath");
        final String clientTrustStoreFilename = config.getProperty("TrustStoreFilepath");
        final boolean withChallenge = config.getProperty("ExtendendAuthWithChallenge").equals("true");
        final String grantType = "client_credentials";
        final String scope = "pub";
        final String aud = "humidity";
        final byte[] secret = (clientID + ":" + clientSecret).getBytes();

        final MqttClientSslConfig sslConfig = getSslConfig(clientKeyFilename, clientTrustStoreFilename, tlsKeyPassword);

        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(clientID)
                .serverHost(rsServerIP)
                .serverPort(rsServerPort)
                .sslConfig(sslConfig)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(asServerIP, asServerPort, secret);
        final TokenRequestResponse token = requestHandler.requestToken(grantType, scope, aud);
        final ACEEnhancedAuthMechanism mechanism = withChallenge ?
                new EnhancedAuthDataMechanismWithAuth(token) :
                new EnhancedAuthDataMechanism(token);
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                // clean start and session expiry interval of 0 as required by the draft
                .cleanStart(true)
                .sessionExpiryInterval(0)
                .enhancedAuth(mechanism)
                .send();
        if (connAck.getReasonCode().equals(Mqtt5ConnAckReasonCode.SUCCESS)) {
            System.out.println("connected :)" + connAck);
        } else {
            System.out.println("failed connection :( " + connAck);
            System.exit(1);
        }
        client.toAsync().publishWith().topic(aud).send().whenComplete((publish, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                System.out.println("Failed publish :( "+ throwable);
            } else {
                System.out.println("Successful publish "+ publish);
            }
            client.disconnect();
        });
    }
}
