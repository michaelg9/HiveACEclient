package com.ace.mqtt.examples.challenge;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.examples.Utils;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        if (args.length == 0) System.exit(1);
        run(args[0]);
    }

    public static Mqtt5Client getClient(final String configFile, final ClientConfig config)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final Mqtt5Client client = new
                AceClientBuilder.Ace5ClientBuilder(config).withAuthentication(
                AceClientBuilder.Ace5ClientBuilder.AuthenticationType.Challenge).connect();
        assert client.getState().isConnected();
        return client;
    }

    public static void run(final String configFile)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        final Mqtt5Client client = getClient(configFile, config);
        Utils.runVersion5Publish(client, config.aud, true);
    }

    public static void run(final String configFile, final int delay)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException, InterruptedException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        while (true) {
            final Mqtt5Client client = getClient(configFile, config);
            client.toBlocking().disconnect();
            Thread.sleep(delay);
        }

    }
}
