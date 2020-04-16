package com.ace.mqtt.examples.simplev5;

import com.ace.mqtt.builder.ClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

import java.util.Arrays;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        if (args.length == 0) System.exit(1);
        run(args[0], 1,1, true);
    }

    private static Mqtt5Client getClient(final ClientConfig config, final boolean withAuthentication)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException {
        final ClientBuilder.Ace5ClientBuilder builder = new ClientBuilder.Ace5ClientBuilder(config);
        final Mqtt5Client client = withAuthentication ? builder.withAuthentication(ClientBuilder.Ace5ClientBuilder.AuthenticationType.UsernamePassword).connect() : builder.connect();
        assert client.getState().isConnected();
        return client;
    }

    public static void run(final String configFile, int repeatIterations, final int repeatDelay, final boolean withAuthentication)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        final long[] times = new long[repeatIterations * 2];
        int iteration = 0;
        while (repeatIterations-- > 0) {
            final long timeStart = System.currentTimeMillis();
            final Mqtt5Client client = getClient(config, withAuthentication);
            final long timeStop = System.currentTimeMillis();
            client.toBlocking().disconnect();
            times[iteration] = timeStart;
            times[iteration + 1] = timeStop;
            iteration += 2;
            Thread.sleep(repeatDelay);
        }
        System.out.println(Arrays.toString(times));
    }
}
