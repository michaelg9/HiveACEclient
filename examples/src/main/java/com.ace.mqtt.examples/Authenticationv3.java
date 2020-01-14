package com.ace.mqtt.examples;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.utils.ClientConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.nimbusds.jose.JOSEException;

import java.io.IOException;

import static com.ace.mqtt.examples.DiscoverAS.getConfigFilename;

public class Authenticationv3 {

    public static void main(final String[] args)
            throws ASUnreachableException, IOException, FailedAuthenticationException, JOSEException {
        final ClientConfig config = ClientConfig.getInstance(getConfigFilename(args));
        final Mqtt3Client client = AceClientBuilder.createV3Client(config).withAuthentication().build();
        final Mqtt3ConnAck connAck = client.toBlocking().connectWith()
                // clean start as required by the draft
                .cleanSession(true)
                .send();
        if (connAck.getReturnCode().equals(Mqtt3ConnAckReturnCode.SUCCESS)) {
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
