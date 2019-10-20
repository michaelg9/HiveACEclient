package com.ace.mqtt.examples;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.http.RequestHandler;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;

public class ExtendedAuthentication {

    public static void main(final String[] args) {
        final RequestHandler requestHandler = new RequestHandler("127.0.0.1", "3001");
        final Mqtt5BlockingClient client = Mqtt5Client.builder()
                .serverHost("127.0.0.1")
                .buildBlocking();
        final Mqtt5ConnAck connAck = client.toBlocking().connectWith()
                .cleanStart(true)
                .enhancedAuth(
                        new EnhancedAuthDataMechanism(
                                "zE*ddCU6cwbFAipf",
                                "7CrGzSyzh1l/2ixRC8XfmVtXWcGDf8+Wuao8yaIsX1w=",
                                requestHandler))
                .send();
        connAck.getAssignedClientIdentifier();
    }
}
