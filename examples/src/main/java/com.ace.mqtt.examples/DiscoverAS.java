package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static com.ace.mqtt.examples.ExtendedAuthentication.getSslConfig;

public class DiscoverAS {

    public static Properties readConfig() throws IOException {
        try (final InputStream input = DiscoverAS.class.getClassLoader().getResourceAsStream("config.properties")) {
            final Properties prop = new Properties();
            if (input == null) {
                throw new IOException("Unable to find config.properties");
            }
            prop.load(input);
            return prop;
        }
    }

    public static void main(final String[] args) throws IOException {
        final Properties config = readConfig();
        final String rsServerIP = config.getProperty("RSServerIP");
        final int rsServerPort = Integer.parseInt(config.getProperty("RSServerPort"));
        final String clientID = config.getProperty("ClientID");
        final char[] tlsKeyPassword = config.getProperty("TLSKeyPassword").toCharArray();
        final String clientKeyFilename = config.getProperty("PrivateKeyStoreFilepath");
        final String clientTrustStoreFilename = config.getProperty("TrustStoreFilepath");

        final MqttClientSslConfig sslConfig = getSslConfig(clientKeyFilename, clientTrustStoreFilename, tlsKeyPassword);

        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(clientID)
                .serverHost(rsServerIP)
                .serverPort(rsServerPort)
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
