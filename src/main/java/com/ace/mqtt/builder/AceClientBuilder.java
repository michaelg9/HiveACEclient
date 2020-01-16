package com.ace.mqtt.builder;

import com.ace.mqtt.auth.ACEEnhancedAuthMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanism;
import com.ace.mqtt.auth.EnhancedAuthDataMechanismWithAuth;
import com.ace.mqtt.auth.EnhancedNoAuthDataMechanism;
import com.ace.mqtt.crypto.MACCalculator;
import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.http.RequestHandler;
import com.ace.mqtt.utils.AuthData;
import com.ace.mqtt.utils.ClientConfig;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.hivemq.client.mqtt.MqttClientBuilderBase;
import com.hivemq.client.mqtt.MqttClientSslConfig;
import com.hivemq.client.mqtt.mqtt3.Mqtt3Client;
import com.hivemq.client.mqtt.mqtt3.Mqtt3ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.nimbusds.jose.JOSEException;
import org.jetbrains.annotations.NotNull;

import static com.ace.mqtt.crypto.SslUtils.getSslConfig;

public final class AceClientBuilder {

    public static abstract class ClientBuilder<T extends MqttClientBuilderBase<T>> {

        final T builder;
        final ClientConfig clientConfig;
        TokenRequestResponse token = null;

        ClientBuilder(final T builder, final ClientConfig clientConfig) {
            this.builder = builder;
            this.clientConfig = clientConfig;
        }

        ClientBuilder<T> withAuthentication()
                throws ASUnreachableException, FailedAuthenticationException, JOSEException {
            final RequestHandler requestHandler =
                    new RequestHandler(clientConfig.asServerIP, clientConfig.asServerPort);
            this.token =
                    requestHandler.requestToken(
                            clientConfig.secret, clientConfig.grantType, clientConfig.scope, clientConfig.aud);
            return this;
        }

        void initClient() {
            builder
                    .identifier(clientConfig.clientID)
                    .serverHost(clientConfig.rsServerIP)
                    .serverPort(clientConfig.rsServerPort)
                    .sslConfig(getSSLConfig(clientConfig));
        }

        private MqttClientSslConfig getSSLConfig(@NotNull final ClientConfig config) {
            return getSslConfig(config.clientKeyFilename,
                    config.clientTrustStoreFilename,
                    config.keyStorePass, config.trustStorePass);
        }
    }

    public static class Ace3ClientBuilder extends ClientBuilder<Mqtt3ClientBuilder> {

        Ace3ClientBuilder(final Mqtt3ClientBuilder builder, final ClientConfig clientConfig) {
            super(builder, clientConfig);
        }

        @Override
        public Ace3ClientBuilder withAuthentication()
                throws JOSEException, ASUnreachableException, FailedAuthenticationException {
            super.withAuthentication();
            final MACCalculator macCalculator = new MACCalculator(
                    token.getCnf().getJwk().getK(),
                    token.getCnf().getJwk().getAlg());
            final byte[] pop = macCalculator.signNonce(token.getAccessToken().getBytes());
            final AuthData authData = new AuthData(token.getAccessToken(), pop);
            this.builder.simpleAuth()
                    .username(authData.getToken())
                    .password(authData.getPOPAuthData())
                    .applySimpleAuth();
            return this;
        }

        public Mqtt3Client build() {
            this.initClient();
            return builder.build();
        }
    }

    public static class Ace5ClientBuilder extends ClientBuilder<Mqtt5ClientBuilder> {

        Ace5ClientBuilder(final Mqtt5ClientBuilder builder, final ClientConfig clientConfig) {
            super(builder, clientConfig);
        }

        public Ace5ClientBuilder withNoAuthentication() {
            builder.enhancedAuth(new EnhancedNoAuthDataMechanism());
            return this;
        }

        public Ace5ClientBuilder withAuthentication(final boolean withChallenge)
                throws JOSEException, ASUnreachableException, FailedAuthenticationException {
            super.withAuthentication();
            final ACEEnhancedAuthMechanism mechanism = withChallenge ?
                    new EnhancedAuthDataMechanismWithAuth(token) :
                    new EnhancedAuthDataMechanism(token);
            builder.enhancedAuth(mechanism);
            return this;
        }

        public Mqtt5Client build() {
            this.initClient();
            return this.builder.build();
        }
    }

    public static Ace5ClientBuilder createV5Client(@NotNull final ClientConfig config) {
        return new Ace5ClientBuilder(Mqtt5Client.builder(), config);
    }

    public static Ace3ClientBuilder createV3Client(@NotNull final ClientConfig config) {
        return new Ace3ClientBuilder(Mqtt3Client.builder(), config);
    }
}
