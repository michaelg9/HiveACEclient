package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.http.RequestHandler;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class ExtendedAuthentication {

    public static void main(final String[] args) {
        final RequestHandler requestHandler = new RequestHandler("127.0.0.1", "3001");
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost("127.0.0.1")
                .buildBlocking();
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                .cleanStart(true)
                .enhancedAuth(
                        new EnhancedAuthDataMechanism(
                                "qdLuPyp2KqcOtdgN",
                                "rBupCxybxhiqlMwxxOya8ixQNS5NV0iW8OWf2tx3Ugo=",
                                requestHandler))
                .send();
        connAck.getAssignedClientIdentifier();
    }
}
