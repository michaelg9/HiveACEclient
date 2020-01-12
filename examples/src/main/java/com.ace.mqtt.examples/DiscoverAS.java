package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.ace.mqtt.crypto.SslUtils;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import io.reactivex.annotations.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;


public class DiscoverAS {

    public static class ClientConfig {

        public final String rsServerIP;
        public final int rsServerPort;
        public final String asServerIP;
        public final String asServerPort;
        public final String clientID;
        public final byte[] clientSecret;
        public final char[] tlsKeyPassword;
        public final String clientKeyFilename;
        public final String clientTrustStoreFilename;
        public final String grantType;
        public final String scope;
        public final String aud;
        public final byte[] secret;
        public final boolean withChallenge;

        public ClientConfig(@NonNull final Properties config) {
            this.rsServerIP = config.getProperty("RSServerIP");
            this.rsServerPort = Integer.parseInt(config.getProperty("RSServerPort"));
            this.asServerIP = config.getProperty("ASServerIP");
            this.asServerPort = config.getProperty("ASServerPort");
            this.clientID = config.getProperty("ClientID");
            String clientSecret = config.getProperty("ClientSecret");
            this.clientSecret = clientSecret.getBytes();
            this.tlsKeyPassword = config.getProperty("TLSKeyPassword").toCharArray();
            this.clientKeyFilename = config.getProperty("PrivateKeyStoreFilepath");
            this.clientTrustStoreFilename = config.getProperty("TrustStoreFilepath");
            this.grantType = "client_credentials";
            this.scope = "pub";
            this.aud = "humidity";
            this.secret = (this.clientID + ":" + clientSecret).getBytes();
            this.withChallenge = Boolean.parseBoolean(config.getProperty("ExtendendAuthWithChallenge"));
            clientSecret = null;
        }

    }
    public static ClientConfig readConfig() throws IOException {
        try (final InputStream input = DiscoverAS.class.getClassLoader().getResourceAsStream("config.properties")) {
            final Properties prop = new Properties();
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            prop.load(input);
            return new ClientConfig(prop);
        }
    }

    public static void main(final String[] args) throws IOException {
        final ClientConfig config = readConfig();

        final MqttClientSslConfig sslConfig =
                SslUtils.getSslConfig(ExtendedAuthentication.class.getResource(config.clientKeyFilename).getFile(),
                        ExtendedAuthentication.class.getResource(config.clientTrustStoreFilename).getFile(),
                        config.tlsKeyPassword);

        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(config.clientID)
                .serverHost(config.rsServerIP)
                .serverPort(config.rsServerPort)
                .sslConfig(sslConfig)
                .buildBlocking();
        String cnonce = null;
        String asServerIP = null;
        try {
            client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(new EnhancedNoAuthDataMechanism())
                    .send();
        } catch (final Mqtt5ConnAckException e) {
            //TODO: parameter names? cnonce use?
            final List<? extends Mqtt5UserProperty> props = e.getMqttMessage().getUserProperties().asList();
            for (final Mqtt5UserProperty p : props) {
                if (p.getName().equals(MqttUtf8String.of("AS"))) {
                    asServerIP = p.getValue().toString();
                } else if (p.getName().equals(MqttUtf8String.of("cnonce"))) {
                    cnonce = p.getValue().toString();
                }
            }
        }
        System.out.println("Discovered server: " + asServerIP);
        if (asServerIP == null) {
            throw new IllegalStateException("Expected to discover the AS server address");
        }
        if (!client.getState().equals(MqttClientState.DISCONNECTED)) {
            client.disconnect();
            throw new IllegalStateException("Client shouldn't be connected");
        }
    }
}
