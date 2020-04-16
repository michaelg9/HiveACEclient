package com.ace.mqtt.examples;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.util.Objects;
import java.util.logging.Logger;

public class Utils {
    private static final Logger LOGGER = Logger.getLogger(Utils.class.getName());

    public static void runVersion5Publish(final Mqtt5Client client, final String topic, final boolean disconnect) {
        assert client.getState().isConnected();
        client.toAsync()
                .publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload("This is a message".getBytes())
                .send()
                .whenComplete((publish, throwable) -> {
                    if (!client.getState().isConnected()) {
                        LOGGER.severe("Failed publish :(  broker disconnected us. Probably authorization error");
                        throw new IllegalStateException("Should have been connected");
                    }
                    if (throwable != null) {
                        LOGGER.severe("Failed publish :( " + throwable);
                    } else {
                        LOGGER.fine("Successful publish " + publish);
                    }
                    if (disconnect) client.toBlocking().disconnect();
                });
    }
}
