package com.ace.mqtt.examples;

import com.ace.mqtt.crypto.AuthCalculator;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import static com.ace.mqtt.examples.DiscoverAS.readConfig;

public class Authenticationv3 {

    public static void main(final String[] args) throws IOException, ASUnreachableException, FailedAuthenticationException {
        final Properties config = readConfig();
        final String rsServerIP = config.getProperty("RSServerIP");
        final String asServerIP = config.getProperty("ASServerIP");
        final String asServerPort = config.getProperty("ASServerPort");
        final String clientID = config.getProperty("ClientID");
        final String clientSecret = config.getProperty("ClientSecret");
        final String grantType = "client_credentials";
        final String scope = "sub";
        final String aud = "humidity";
        final byte[] secret = (clientID+":"+clientSecret).getBytes();
        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .identifier(clientID)
                .serverHost(rsServerIP)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(asServerIP, asServerPort, secret);
        final TokenRequestResponse token = requestHandler.requestToken(grantType, scope, aud);
        final ByteBuffer password = new AuthCalculator(
                token.getCnf().getJwk().getK(),
                token.getAccess_token(),
                token.getCnf().getJwk().getAlg()).signNonce(token.getAccess_token().getBytes());
        final Mqtt3ConnAck connAck = client.toBlocking().connectWith()
                // clean start as required by the draft
                .cleanSession(true)
                .simpleAuth().username(token.getAccess_token()).password(password).applySimpleAuth()
                .send();
        System.out.println("connected " + connAck);
        client.disconnect();
    }
}
