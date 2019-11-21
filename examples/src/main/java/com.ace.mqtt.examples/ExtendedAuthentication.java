package com.ace.mqtt.examples;

import com.ace.mqtt.auth.ACEEnhancedAuthMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanismWithAuth;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

import java.io.IOException;
import java.util.Properties;

import static com.ace.mqtt.examples.DiscoverAS.readConfig;

public class ExtendedAuthentication {

    public static void main(final String[] args)
            throws IOException, ASUnreachableException, FailedAuthenticationException {
        final Properties config = readConfig();
        final String rsServerIP = config.getProperty("RSServerIP");
        final String asServerIP = config.getProperty("ASServerIP");
        final String asServerPort = config.getProperty("ASServerPort");
        final String clientID = config.getProperty("ClientID");
        final String clientSecret = config.getProperty("ClientSecret");
        final boolean withChallenge = config.getProperty("ExtendendAuthWithChallenge").equals("true");
        final String grantType = "client_credentials";
        final String scope = "sub";
        final String aud = "humidity";
        final byte[] secret = (clientID+":"+clientSecret).getBytes();
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(clientID)
                .serverHost(rsServerIP)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(asServerIP, asServerPort, secret);
        final TokenRequestResponse token = requestHandler.requestToken(grantType, scope, aud);
        final ACEEnhancedAuthMechanism mechanism = withChallenge ?
                new EnhancedAuthDataMechanismWithAuth(token) :
                new EnhancedAuthDataMechanism(token);
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                .cleanStart(true)
                .sessionExpiryInterval(30)
                .enhancedAuth(mechanism)
                .send();
        System.out.println("connected " + connAck);
        client.disconnect();
    }
}
