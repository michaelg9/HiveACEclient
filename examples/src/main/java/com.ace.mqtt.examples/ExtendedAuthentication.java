package com.ace.mqtt.examples;

import com.ace.mqtt.auth.ACEEnhancedAuthMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanismWithAuth;
import com.ace.mqtt.http.RequestHandler;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class ExtendedAuthentication {

    public static void main(final String[] args) {
        if (args.length < 2) {
            System.out.println("Parameters: client-id client-secret [\"challenge\"]");
            System.exit(1);
        }
        final String rsServer = "127.0.0.1";
        final String asServer = "127.0.0.1";
        final String asPort = "3001";
        final String clientID = args[0];
        final String clientSecret = args[1];
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(clientID)
                .serverHost(rsServer)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(asServer, asPort, (clientID+":"+clientSecret).getBytes());
        final ACEEnhancedAuthMechanism mechanism;
        if (args.length == 3 && args[2].equals("challenge")) {
            mechanism = new EnhancedAuthDataMechanismWithAuth(requestHandler);
        } else {
            mechanism = new EnhancedAuthDataMechanism(requestHandler);
        }
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                .cleanStart(true)
                .sessionExpiryInterval(30)
                .enhancedAuth(mechanism)
                .send();
        System.out.println("connected " + connAck);
        client.disconnect();
    }
}
