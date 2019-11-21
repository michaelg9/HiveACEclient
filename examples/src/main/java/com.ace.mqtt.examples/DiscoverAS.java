package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    public static void main(final String[] args)
            throws IOException, ASUnreachableException, FailedAuthenticationException {
        final Properties config = readConfig();
        final String rsServerIP = config.getProperty("RSServerIP");
        final String asServerPort = config.getProperty("ASServerPort");
        final String clientID = config.getProperty("ClientID");
        final String clientSecret = config.getProperty("ClientSecret");
        final byte[] secret = (clientID+":"+clientSecret).getBytes();
        final String grantType = "client_credentials";
        final String scope = "sub";
        final String aud = "humidity";
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(clientID)
                .serverHost(rsServerIP)
                .buildBlocking();
        Mqtt5ConnAck connAck;
        try {
            connAck = client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(new EnhancedNoAuthDataMechanism())
                    .send();
        } catch (final Mqtt5ConnAckException e) {
            final String asServerIP = e.getMqttMessage().getReasonString().orElseThrow().toString();
            final RequestHandler requestHandler = new RequestHandler(asServerIP, asServerPort, secret);
            final TokenRequestResponse token = requestHandler.requestToken(grantType, scope, aud);
            connAck = client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(new EnhancedAuthDataMechanism(token))
                    .send();
        }
        System.out.println("connected " + connAck);
        client.disconnect();
    }
}
