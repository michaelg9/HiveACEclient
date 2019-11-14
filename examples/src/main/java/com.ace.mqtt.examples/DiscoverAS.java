package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.ace.mqtt.http.RequestHandler;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class DiscoverAS {

    public static void main(final String[] args) {
        if (args.length != 2) {
            System.out.println("Need to pass client id as first parameter and client secret as second parameter");
            System.exit(1);
        }
        final String rsServer = "127.0.0.1";
        final String rsServerPort = "3001";
        final String clientID = args[0];
        final String clientSecret = args[1];
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(clientID)
                .serverHost(rsServer)
                .buildBlocking();
        Mqtt5ConnAck connAck;
        try {
            connAck = client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(new EnhancedNoAuthDataMechanism())
                    .send();
        } catch (final Mqtt5ConnAckException e) {
            final String asServerIP = e.getMqttMessage().getReasonString().orElseThrow().toString();
            final RequestHandler requestHandler = new RequestHandler(asServerIP, rsServerPort, (clientID+":"+clientSecret).getBytes());
            connAck = client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(new EnhancedAuthDataMechanism(requestHandler))
                    .send();
        }
        System.out.println("connected " + connAck);
        client.disconnect();
    }
}
