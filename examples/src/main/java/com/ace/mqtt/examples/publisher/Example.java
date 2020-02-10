package com.ace.mqtt.examples.publisher;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

import static com.ace.mqtt.examples.Utils.runVersion5Publish;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        if (args.length == 0) System.exit(1);
        run(args[0]);
    }

    public static void run(final String configFile)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        run(configFile, 60000 * 5);
    }

    public static void run(final String configFile, final int delay)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        final Mqtt5Client client = new
                AceClientBuilder.Ace5ClientBuilder(config).withAuthentication(
                AceClientBuilder.Ace5ClientBuilder.AuthenticationType.Challenge).connect();
        while (true) {
            assert client.getState().isConnected();
            runVersion5Publish(client, config.aud, false);
            Thread.sleep(delay);
        }
    }
}
