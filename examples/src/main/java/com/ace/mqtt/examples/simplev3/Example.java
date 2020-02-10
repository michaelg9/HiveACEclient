package com.ace.mqtt.examples.simplev3;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.nimbusds.jose.JOSEException;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        if (args.length == 0) System.exit(1);
        run(args[0]);
    }

    private static Mqtt3Client getClient(final ClientConfig config)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException {
        final Mqtt3Client client = new AceClientBuilder.Ace3ClientBuilder(config)
                .withAuthentication()
                .connect();
        assert client.getState().isConnected();
        return client;
    }

    public static void run(final String configFile)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        final Mqtt3Client client =  getClient(config);
        publish(client, config);
    }

    public static void run(final String configFile, final int livePeriod)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        while (true) {
            final Mqtt3Client client =  getClient(config);
            client.toBlocking().disconnect();
            Thread.sleep(livePeriod);
        }
    }

    private static void publish(final Mqtt3Client client, final ClientConfig config) {
        client.toAsync()
                .publishWith()
                .topic(config.aud)
                .payload("This is a message to the world".getBytes())
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete((publish, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        System.out.println("Failed publish :( " + throwable);
                    } else if (!client.getState().isConnected()) {
                        System.out.println("Failed publish :(  broker disconnected us. Probably authorization error");
                    } else {
                        System.out.println("Successful publish " + publish);
                    }
                    if (client.getState().isConnected()) {
                        client.toBlocking().disconnect();
                    } else {
                        throw new IllegalStateException("Should have been connected");
                    }
                });
    }
}
