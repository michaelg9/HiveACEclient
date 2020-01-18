package com.ace.mqtt.examples;

import com.ace.mqtt.builder.AceClientBuilder;
import com.ace.mqtt.config.ClientConfig;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;

import java.util.List;

import static com.ace.mqtt.utils.Constants.CONFIG_ENV_NAME;

public class DiscoverAS {

    public static String getConfigFilename(final String[] args) {
        String result = null;
        if (args.length > 0) result = args[0];
        else if (System.getProperty(CONFIG_ENV_NAME) != null) return System.getProperty(CONFIG_ENV_NAME);
        if (result == null) {
            throw new RuntimeException("Unable to find client config");
        }
        return result;
    }

    public static void main(final String[] args) {
        final ClientConfig config = ClientConfig.getInstance(getConfigFilename(args));
        final Mqtt5Client client = AceClientBuilder.createV5Client(config).discoverASServer().build();
        String cnonce = null;
        String asServerIP = null;
        try {
            client.toBlocking().connectWith()
                    .cleanStart(true)
                    .sessionExpiryInterval(0)
                    .send();
        } catch (final Mqtt5ConnAckException e) {
            //TODO: parameter names? cnonce use?
            final List<? extends Mqtt5UserProperty> props = e.getMqttMessage().getUserProperties().asList();
            for (final Mqtt5UserProperty p : props) {
                if (p.getName().equals(MqttUtf8String.of("AS"))) {
                    asServerIP = p.getValue().toString();
                } else if (p.getName().equals(MqttUtf8String.of("cnonce"))) {
                    cnonce = p.getValue().toString();
                }
            }
        }
        if (!client.getState().equals(MqttClientState.DISCONNECTED)) {
            client.toBlocking().disconnect();
            throw new IllegalStateException("Client shouldn't be connected");
        }
        if (asServerIP == null) {
            throw new IllegalStateException("Expected to discover the AS server address");
        }
        System.out.println("Discovered server: " + asServerIP);
        config.setAsServerIP(asServerIP);
    }
}
