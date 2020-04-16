package com.ace.mqtt.examples.publisher;

import com.ace.mqtt.builder.ClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.nimbusds.jose.JOSEException;

import java.util.Arrays;

import static com.ace.mqtt.examples.Utils.runVersion5Publish;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        if (args.length == 0) System.exit(1);
        run(args[0], 110, 2000, true);
    }

    public static void run(final String configFile)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        run(configFile, 1, 1, true);
    }

    public static void run(final String configFile, int repeatIterations, final int repeatDelay, final boolean withAuth)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        final ClientConfig config = ClientConfig.getInstance(configFile);
        config.setScope("pub");
        final long[] times = new long[repeatIterations * 2];
        int iteration = 0;
        final ClientBuilder.Ace5ClientBuilder builder = new ClientBuilder.Ace5ClientBuilder(config);
        final Mqtt5Client client = !withAuth ? builder.connect() : builder.withAuthentication(ClientBuilder.Ace5ClientBuilder.AuthenticationType.Challenge).connect();
        while (repeatIterations -- > 0) {
            assert client.getState().isConnected();
            final long timeStart = System.currentTimeMillis();
            runVersion5Publish(client, config.aud, false);
            final long timeStop = System.currentTimeMillis();
            times[iteration] = timeStart;
            times[iteration + 1] = timeStop;
            iteration += 2;
            Thread.sleep(repeatDelay);
        }
        client.toBlocking().disconnect();
        System.out.println(Arrays.toString(times));
    }
}
