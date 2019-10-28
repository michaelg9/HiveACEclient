package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.http.RequestHandler;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class ExtendedAuthentication {

    public static void main(final String[] args) {
        final String rsServer = "127.0.0.1";
        final String asServer = "127.0.0.1";
        final String asPort = "3001";
        final String clientID = "zE*ddCU6cwbFAipf";
        final String clientSecret = "7CrGzSyzh1l/2ixRC8XfmVtXWcGDf8+Wuao8yaIsX1w=";
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost(rsServer)
                .buildBlocking();
        final RequestHandler requestHandler = new RequestHandler(asServer, asPort, clientID, clientSecret);
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                .cleanStart(true)
                .sessionExpiryInterval(30)
                .enhancedAuth(new EnhancedAuthDataMechanism(requestHandler))
                .send();
        System.out.println("connected " + connAck);
        client.disconnect();
    }
}
