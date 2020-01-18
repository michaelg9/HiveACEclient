package com.ace.mqtt.examples;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.config.ClientConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;
import com.nimbusds.jose.JOSEException;

import static com.ace.mqtt.examples.DiscoverAS.getConfigFilename;

public class ExtendedAuthentication {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(getConfigFilename(args));
        final Mqtt5Client client = AceClientBuilder.createV5Client(config).withAuthentication(true).build();
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                // clean start and session expiry interval of 0 as required by the draft
                .cleanStart(true)
                .sessionExpiryInterval(0)
                .send();
        if (connAck.getReasonCode().equals(Mqtt5ConnAckReasonCode.SUCCESS)) {
            System.out.println("connected :)" + connAck);
        } else {
            System.out.println("failed connection :( " + connAck);
        }
        client.toAsync().publishWith().topic(config.aud).send().whenComplete((publish, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                System.out.println("Failed publish :( "+ throwable);
            } else if (!client.getState().isConnected()) {
                System.out.println("Failed publish :(  broker disconnected us. Probably authorization error");
            } else {
                System.out.println("Successful publish "+ publish);
            }
            if (client.getState().isConnected()) client.toBlocking().disconnect();
        });
    }
}
