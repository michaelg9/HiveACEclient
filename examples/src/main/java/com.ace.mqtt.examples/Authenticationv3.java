package com.ace.mqtt.examples;

import com.ace.mqtt.crypto.MACCalculator;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.AuthData;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3BlockingClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck;
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAckReturnCode;
import com.nimbusds.jose.JOSEException;

import java.io.IOException;

import static com.ace.mqtt.crypto.SslUtils.getSslConfig;
import static com.ace.mqtt.examples.DiscoverAS.readConfig;

public class Authenticationv3 {

    public static void main(final String[] args)
            throws ASUnreachableException, IOException, FailedAuthenticationException, JOSEException {
        final DiscoverAS.ClientConfig config = readConfig();

        final MqttClientSslConfig sslConfig =
                getSslConfig(ExtendedAuthentication.class.getResource(config.clientKeyFilename).getFile(),
                        ExtendedAuthentication.class.getResource(config.clientTrustStoreFilename).getFile(),
                        config.tlsKeyPassword);

        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .identifier(config.clientID)
                .serverHost(config.rsServerIP)
                .serverPort(config.rsServerPort)
                .sslConfig(sslConfig)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(config.asServerIP, config.asServerPort, config.secret);
        final TokenRequestResponse token = requestHandler.requestToken(config.grantType, config.scope, config.aud);
        final MACCalculator macCalculator = new MACCalculator(
                token.getCnf().getJwk().getK(),
                token.getCnf().getJwk().getAlg());
        final byte[] pop = macCalculator.signNonce(token.getAccess_token().getBytes());
        final AuthData authData = new AuthData(token.getAccess_token(), pop);
        final Mqtt3ConnAck connAck = client.toBlocking().connectWith()
                // clean start as required by the draft
                .cleanSession(true)
                .simpleAuth().username(token.getAccess_token()).password(authData.getPOPAuthData()).applySimpleAuth()
                .send();
        if (connAck.getReturnCode().equals(Mqtt3ConnAckReturnCode.SUCCESS)) {
            System.out.println("connected :)" + connAck);
        } else {
            System.out.println("failed connection :( " + connAck);
            System.exit(1);
        }
        client.toAsync().publishWith().topic(config.aud).send().whenComplete((publish, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                System.out.println("Failure :( "+ throwable);
            } else {
                System.out.println("Success "+ publish);
            }
            client.disconnect();
        });
    }

}
