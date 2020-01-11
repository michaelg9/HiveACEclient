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
import java.util.Properties;

import static com.ace.mqtt.examples.DiscoverAS.readConfig;
import static com.ace.mqtt.examples.ExtendedAuthentication.getSslConfig;

public class Authenticationv3 {

    public static void main(final String[] args)
            throws ASUnreachableException, IOException, FailedAuthenticationException, JOSEException {
        final Properties config = readConfig();
        final String rsServerIP = config.getProperty("RSServerIP");
        final int rsServerPort = Integer.parseInt(config.getProperty("RSServerPort"));
        final String asServerIP = config.getProperty("ASServerIP");
        final String asServerPort = config.getProperty("ASServerPort");
        final String clientID = config.getProperty("ClientID");
        final String clientSecret = config.getProperty("ClientSecret");
        final char[] tlsKeyPassword = config.getProperty("TLSKeyPassword").toCharArray();
        final String clientKeyFilename = config.getProperty("PrivateKeyStoreFilepath");
        final String clientTrustStoreFilename = config.getProperty("TrustStoreFilepath");
        final String grantType = "client_credentials";
        final String scope = "pub";
        final String aud = "humidity";
        final byte[] secret = (clientID + ":" + clientSecret).getBytes();

        final MqttClientSslConfig sslConfig = getSslConfig(clientKeyFilename, clientTrustStoreFilename, tlsKeyPassword);

        final Mqtt3BlockingClient client = Mqtt3Client.builder()
                .identifier(clientID)
                .serverHost(rsServerIP)
                .serverPort(rsServerPort)
                .sslConfig(sslConfig)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(asServerIP, asServerPort, secret);
        final TokenRequestResponse token = requestHandler.requestToken(grantType, scope, aud);
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
        client.toAsync().publishWith().topic(aud).send().whenComplete((publish, throwable) -> {
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
