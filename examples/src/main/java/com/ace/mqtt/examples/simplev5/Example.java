package com.ace.mqtt.examples.simplev5;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

import static com.ace.mqtt.examples.Utils.runVersion5Publish;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        if (args.length == 0) System.exit(1);
        run(args[0]);
    }

    private static Mqtt5Client getClient(final ClientConfig config)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException {
        final Mqtt5Client client = new
                AceClientBuilder.Ace5ClientBuilder(config).withAuthentication(
                AceClientBuilder.Ace5ClientBuilder.AuthenticationType.UsernamePassword).connect();
        assert client.getState().isConnected();
        return client;
    }

    public static void run(final String configFile, final int livePeriod)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        while (true) {
            final Mqtt5Client client = getClient(config);
            client.toBlocking().disconnect();
            Thread.sleep(livePeriod);
        }
    }

    public static void run(final String configFile)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        final Mqtt5Client client = getClient(config);
        runVersion5Publish(client, config.aud, true);
    }
}
