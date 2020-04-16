package com.ace.mqtt.examples.subscriber;

import com.ace.mqtt.builder.ClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

public class Example {

    private static final Logger LOGGER = Logger.getLogger(Example.class.getName());

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        if (args.length == 0) System.exit(1);
        run(args[0], true);
    }

    public static void run(final String configFile, final boolean withAuth)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        config.setScope("sub");
        final ClientBuilder.Ace5ClientBuilder builder = new ClientBuilder.Ace5ClientBuilder(config);
        final Mqtt5Client client = withAuth ? builder.withAuthentication(ClientBuilder.Ace5ClientBuilder.AuthenticationType.Challenge).connect() : builder.connect();
        runVersion5Subscribe(client, config);
    }

    private static void runVersion5Subscribe(final Mqtt5Client client, final ClientConfig config) {
        assert client.getState().isConnected();
        final ArrayList<Long> times = new ArrayList<>();
        get();
        client.toAsync()
                .subscribeWith()
                .topicFilter(config.aud)
                .noLocal(true)
                .qos(MqttQos.AT_LEAST_ONCE)
                .callback(publish -> {
                    final long time = System.currentTimeMillis();
                    times.add(time);
                    LOGGER.info(String.format("Iteration: %d Message %s at %d", times.size(),
                            publish.getPayload().isPresent() ? new String(publish.getPayloadAsBytes()) : "Empty", time));
                    if (times.size() == 100)
                        System.out.println(Arrays.toString(times.toArray()));
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
                        LOGGER.fine("Successful subscribe " + publish);
                    }
                });
    }

    private static void get()  {
        final Field masterSecretField;
        try {
            final Class<?> c = Class.forName("sun.security.ssl.SSLSessionImpl");
            masterSecretField = c.getDeclaredField("masterSecret");
            masterSecretField.setAccessible(true);
            System.out.println(masterSecretField);
        } catch (final NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
//        final SecretKey k = (SecretKey)masterSecretField.get(session);
//        System.out.println("secret: " + DatatypeConverter.printHexBinary(k.getEncoded()).toLowerCase());

    }
}
