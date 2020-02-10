package com.ace.mqtt.builder;

import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanismWithAuth;
import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.ace.mqtt.config.ClientConfig;
import com.ace.mqtt.crypto.MACCalculator;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.exceptions.UnregisteredClientException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.AuthData;
import com.ace.mqtt.utils.dataclasses.ClientRegistrationResponse;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.MqttClientBuilderBase;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.datatypes.MqttUtf8String;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3Connect;
import com.hivemq.client.mqtt.mqtt3.message.connect.Mqtt3ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperty;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck;
import com.nimbusds.jose.JOSEException;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static com.ace.mqtt.crypto.SslUtils.encryptUsingPK;
import static com.ace.mqtt.crypto.SslUtils.getSslConfig;

public final class AceClientBuilder {

    private final static Logger LOGGER = Logger.getLogger(AceClientBuilder.class.getName());

    public static abstract class ClientBuilder<T extends MqttClientBuilderBase<T>> {

        final T builder;
        final ClientConfig clientConfig;

        ClientBuilder(final T builder, final ClientConfig clientConfig) {
            this.builder = builder;
            this.clientConfig = clientConfig;
        }

        private void registerClient(final RequestHandler requestHandler)
                throws ASUnreachableException, FailedAuthenticationException {
            if (clientConfig.isClientRegistered()) {
                throw new IllegalStateException("Attempting to re-register already registered user");
            }
            if (!clientConfig.isASInfoAvailable()) {
                throw new IllegalStateException("Attempting to register client before discovering AS server");
            }
            LOGGER.info("Client not registered. Attempting to register..");
            if (!clientConfig.canClientRegister()) {
                LOGGER.severe("Client username or password missing. Unable to register client");
                throw new UnregisteredClientException(
                        "Client is unregistered and no registration credentials found");
            }
            final ClientRegistrationResponse response =
                    requestHandler.registerClient(
                            Objects.requireNonNull(clientConfig.clientUsername),
                            Objects.requireNonNull(clientConfig.clientUri));
            clientConfig.registerClient(response);

        }

        private TokenRequestResponse requestToken(final RequestHandler requestHandler)
                throws ASUnreachableException, FailedAuthenticationException {
            if (!clientConfig.isClientRegistered()) {
                registerClient(requestHandler);
            }
            LOGGER.info("Requesting token from AS");
            return requestHandler.requestToken(
                    clientConfig.getHTTPAuthSecret(), clientConfig.grantType, clientConfig.getScope(),
                    clientConfig.aud);
        }

        TokenRequestResponse requestToken()
                throws ASUnreachableException, FailedAuthenticationException {
            final RequestHandler requestHandler =
                    new RequestHandler(
                            Objects.requireNonNull(clientConfig.asServerIP),
                            Objects.requireNonNull(clientConfig.asServerPort));
            return requestToken(requestHandler);
        }

        AuthData getAuthData()
                throws ASUnreachableException, FailedAuthenticationException, JOSEException {
            if (!clientConfig.isASInfoAvailable()) {
                throw new ASUnreachableException("Unable to discover AS server");
            }
            return this.getAuthDataFromToken(requestToken());
        }

        AuthData getEncryptedAuthData()
                throws JOSEException, ASUnreachableException, FailedAuthenticationException {
            final AuthData authData = getAuthData();
            final String trustStoreFilename = clientConfig.getClientTrustStoreFilename().toString();
            try {
                authData.setToken(new String(
                        encryptUsingPK(
                                trustStoreFilename, clientConfig.trustStorePass, authData.getToken().getBytes())));
                authData.setPop(encryptUsingPK(trustStoreFilename, clientConfig.trustStorePass, authData.getPOP()));
            } catch (final Exception e) {
                e.printStackTrace();
                throw new IllegalStateException("Unable to encrypt auth data using broker's public key");
            }
            return authData;
        }

        private AuthData getAuthDataFromToken(final TokenRequestResponse token) throws JOSEException {
            final MACCalculator macCalculator = new MACCalculator(
                    token.getCnf().getJwk().getK(),
                    token.getCnf().getJwk().getAlg());
            final byte[] pop = macCalculator.signNonce(token.getAccessToken().getBytes());
            return new AuthData(token.getAccessToken(), pop);
        }

        void initClient() {
            builder.serverHost(clientConfig.rsServerIP);
            if (clientConfig.clientID != null) {
                builder.identifier(clientConfig.clientID);
            }
            if (clientConfig.transportType.equals(ClientConfig.TRANSPORTTYPE.TLS)) {
                withSSLTransport();
            } else if (clientConfig.transportType.equals(ClientConfig.TRANSPORTTYPE.TCP)){
                withoutSSLTransport();
            } else {

            }

        }

        private void withSSLTransport() {
            builder.sslConfig(getSSLConfig(clientConfig)).serverPort(clientConfig.rsServerTLSPort);
            LOGGER.info("TLS Transport setup");
        }

        private void withoutSSLTransport() {
            builder.sslConfig(null).serverPort(clientConfig.rsServerTCPPort);
            LOGGER.info("TCP Transport setup");
        }

        private MqttClientSslConfig getSSLConfig(@NotNull final ClientConfig config) {
            return getSslConfig(config.getClientKeyFilename().toString(),
                    config.getClientTrustStoreFilename().toString(),
                    config.keyStorePass, config.trustStorePass);
        }
    }

    public static class Ace3ClientBuilder extends ClientBuilder<Mqtt3ClientBuilder> {

        private final Mqtt3ConnectBuilder connectBuilder = Mqtt3Connect.builder().cleanSession(true);

        public Ace3ClientBuilder(@NotNull final ClientConfig config) {
            super(Mqtt3Client.builder(), config);
        }

        public Ace3ClientBuilder withAuthentication()
                throws JOSEException, ASUnreachableException, FailedAuthenticationException {
            final AuthData authData = super.getAuthData();
            this.connectBuilder.simpleAuth()
                    .username(authData.getTokenEncoded())
                    .password(authData.getPOPAuthData())
                    .applySimpleAuth();
            LOGGER.info("Username and Password authentication applied to v3 client");
            return this;
        }

        public Mqtt3Client connect() {
            this.initClient();
            final Mqtt3Client client = this.builder.build();
            LOGGER.info("Connecting to broker");
            client.toBlocking().connect(connectBuilder.build());
            if (client.getState().isConnected()) {
                LOGGER.info("Connected successfully");
            } else {
                LOGGER.info("Connection failed!");
            }
            return client;
        }
    }

    public static class Ace5ClientBuilder extends ClientBuilder<Mqtt5ClientBuilder> {

        private final Mqtt5ConnectBuilder connectBuilder =
                Mqtt5Connect.builder().cleanStart(true).sessionExpiryInterval(0);
        private AuthenticationType pendingAuthentication = null;

        public enum AuthenticationType {
            UsernamePassword,
            Challenge,
            AuthenticationData
        }

        public Ace5ClientBuilder(final ClientConfig clientConfig) {
            super(Mqtt5Client.builder(), clientConfig);
        }

        public Ace5ClientBuilder withAuthentication(final AuthenticationType type)
                throws ASUnreachableException, FailedAuthenticationException, JOSEException {
            if (!clientConfig.isASInfoAvailable()) {
                LOGGER.info("No AS server found. Attempting a discovery connect");
                connectBuilder.enhancedAuth(new EnhancedNoAuthDataMechanism());
                pendingAuthentication = type;
            } else if (AuthenticationType.Challenge.equals(type)) {
                final TokenRequestResponse token = requestToken();
                connectBuilder.enhancedAuth(new EnhancedAuthDataMechanismWithAuth(token));
                LOGGER.info("Challenge auth applied");
            } else if (AuthenticationType.AuthenticationData.equals(type)) {
                final TokenRequestResponse token = requestToken();
                connectBuilder.enhancedAuth(new EnhancedAuthDataMechanism(token));
                LOGGER.info("Enhanced auth applied");
            } else {
                connectBuilder.enhancedAuth(new EnhancedNoAuthDataMechanism());
                final AuthData authData = super.getAuthData();
                connectBuilder.simpleAuth()
                        .username(authData.getTokenEncoded())
                        .password(authData.getPOPAuthData())
                        .applySimpleAuth();
                LOGGER.info("Username Password auth for v5 client applied");
            }
            return this;
        }

        private URL extractASlocator(final Mqtt5ConnAck message) {
            LOGGER.info("Extracting AS server information from discovery CONNACK");
            final List<? extends Mqtt5UserProperty> props = message.getUserProperties().asList();
            String asServer = null;
            for (final Mqtt5UserProperty p : props) {
                if (p.getName().equals(MqttUtf8String.of("AS"))) {
                    asServer = p.getValue().toString();
                    break;
                }
            }
            final URL asServerURL;
            try {
                if (asServer == null) {
                    throw new MalformedURLException("Expected to received AS server information");
                }
                asServerURL = new URL(asServer);
            } catch (final MalformedURLException e) {
                LOGGER.severe(String.format("Unable to retrieve AS location from %s : %s ", asServer, e.getMessage()));
                throw new RuntimeException("Expected a valid URI of AS server.");
            }
            LOGGER.info(String.format("Extracted AS server information from CONNACK: %s", asServerURL));
            return asServerURL;
        }

        public Mqtt5Client connect() {
            this.initClient();
            final Mqtt5Client client = this.builder.build();
            final URL asServer;
            Mqtt5ConnAckException error = null;
            try {
                LOGGER.info("Connecting to broker");
                client.toBlocking().connect(connectBuilder.build());
            } catch (final Mqtt5ConnAckException e) {
                error = e;
                LOGGER.info(String.format("Received disconnect CONNACK %s", e.getMqttMessage().getReasonString()));
            }
            if (error != null) {
                if (pendingAuthentication == null) {
                    throw error;
                }
                final AuthenticationType type = pendingAuthentication;
                pendingAuthentication = null;
                //TODO: parameter names? cnonce use?
                asServer = extractASlocator(error.getMqttMessage());
                if (asServer == null) {
                    throw error;
                }
                clientConfig.setAsServerIP(asServer.getHost());
                clientConfig.setAsServerPort(Integer.toString(asServer.getPort()));
                try {
                    LOGGER.info("Retrying connection");
                    return this.withAuthentication(type).connect();
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    throw new IllegalStateException("Unable to reconnect after identifying AS server");
                }
            }
            if (client.getState().isConnected()) {
                LOGGER.info("Connected successfully");
            } else {
                LOGGER.info("Connection failed!");
            }
            return client;
        }
    }
}
