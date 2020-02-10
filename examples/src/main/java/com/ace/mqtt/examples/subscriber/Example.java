package com.ace.mqtt.examples.subscriber;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

import java.util.logging.Logger;

public class Example {

    private static final Logger LOGGER = Logger.getLogger(Example.class.getName());

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        if (args.length == 0) System.exit(1);
        run(args[0]);
    }

    public static void run(final String configFile)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        config.setScope("sub");
        final Mqtt5Client client = new
                AceClientBuilder.Ace5ClientBuilder(config).withAuthentication(
                AceClientBuilder.Ace5ClientBuilder.AuthenticationType.Challenge).connect();
        runVersion5Subscribe(client, config);
    }

    private static void runVersion5Subscribe(final Mqtt5Client client, final ClientConfig config) {
        assert client.getState().isConnected();
        client.toAsync()
                .subscribeWith()
                .topicFilter(config.aud)
                .noLocal(true)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    LOGGER.info(String.format(
                            "received message: %s",
                            publish.getPayload().isPresent() ? new String(publish.getPayloadAsBytes()) : "Empty"));
                })
                .send()
                .whenComplete((publish, throwable) -> {
                    if (!client.getState().isConnected()) {
                        LOGGER.severe("Client was disconnected. Exiting..");
                        throw new IllegalStateException("Should have been connected");
                    }
                    if (throwable != null) {
                        LOGGER.severe("Failed subscribe :( " + throwable);
                    } else {
                        LOGGER.info("Successful subscribe " + publish);
                    }
                });
    }
}
