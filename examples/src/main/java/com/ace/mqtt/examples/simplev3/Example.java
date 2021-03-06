package com.ace.mqtt.examples.simplev3;

import com.ace.mqtt.builder.ClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.nimbusds.jose.JOSEException;

import java.util.Arrays;

public class Example {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException, InterruptedException {
        if (args.length == 0) System.exit(1);
        run(args[0], 1,1, true);
    }

    private static Mqtt3Client getClient(final ClientConfig config, final boolean withAuthentication)
            throws JOSEException, ASUnreachableException, FailedAuthenticationException {
        final ClientBuilder.Ace3ClientBuilder builder = new ClientBuilder.Ace3ClientBuilder(config);
        final Mqtt3Client client = withAuthentication ? builder.withAuthentication(true).connect() : builder.connect();
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
            final Mqtt3Client client =  getClient(config, withAuthentication);
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
