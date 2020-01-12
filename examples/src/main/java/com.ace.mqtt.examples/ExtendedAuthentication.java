package com.ace.mqtt.examples;

import com.ace.mqtt.auth.ACEEnhancedAuthMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanismWithAuth;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAckReasonCode;

import java.io.IOException;

import static com.ace.mqtt.crypto.SslUtils.getSslConfig;
import static com.ace.mqtt.examples.DiscoverAS.readConfig;

public class ExtendedAuthentication {

    public static void main(final String[] args)
            throws ASUnreachableException, FailedAuthenticationException, IOException {
        final DiscoverAS.ClientConfig config = readConfig();

        final MqttClientSslConfig sslConfig =
                getSslConfig(ExtendedAuthentication.class.getResource(config.clientKeyFilename).getFile(),
                ExtendedAuthentication.class.getResource(config.clientTrustStoreFilename).getFile(),
                        config.tlsKeyPassword);

        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(config.clientID)
                .serverHost(config.rsServerIP)
                .serverPort(config.rsServerPort)
                .sslConfig(sslConfig)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(config.asServerIP, config.asServerPort, config.secret);
        final TokenRequestResponse token = requestHandler.requestToken(config.grantType, config.scope, config.aud);
        final ACEEnhancedAuthMechanism mechanism = config.withChallenge ?
                new EnhancedAuthDataMechanismWithAuth(token) :
                new EnhancedAuthDataMechanism(token);
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                // clean start and session expiry interval of 0 as required by the draft
                .cleanStart(true)
                .sessionExpiryInterval(0)
                .enhancedAuth(mechanism)
                .send();
        if (connAck.getReasonCode().equals(Mqtt5ConnAckReasonCode.SUCCESS)) {
            System.out.println("connected :)" + connAck);
        } else {
            System.out.println("failed connection :( " + connAck);
            System.exit(1);
        }
        client.toAsync().publishWith().topic(config.aud).send().whenComplete((publish, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                System.out.println("Failed publish :( "+ throwable);
            } else {
                System.out.println("Successful publish "+ publish);
            }
            client.disconnect();
        });
    }
}
