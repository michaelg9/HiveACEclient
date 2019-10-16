package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.ace.mqtt.http.RequestHandler;
import com.hivemq.client.internal.mqtt.datatypes.MqttUtf8StringImpl;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class DiscoverAS {

    public static void main(final String[] args) {
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost("127.0.0.1")
                .buildBlocking();
        Mqtt5ConnAck connAck;
        try {
            connAck = client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(new EnhancedNoAuthDataMechanism())
                    .send();
        } catch (final Mqtt5ConnAckException e) {
            final String asServerIP = e.getMqttMessage().getReasonString().orElse(MqttUtf8StringImpl.of("")).toString();
            final RequestHandler requestHandler = new RequestHandler(asServerIP, "3001");
            connAck = client.toBlocking().connectWith()
                    .cleanStart(true)
                    .enhancedAuth(
                            new EnhancedAuthDataMechanism(
                                    "qdLuPyp2KqcOtdgN",
                                    "rBupCxybxhiqlMwxxOya8ixQNS5NV0iW8OWf2tx3Ugo=",
                                    requestHandler))
                    .send();
        }
        connAck.getAssignedClientIdentifier();
    }
}
