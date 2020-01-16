package com.ace.mqtt.http;

import com.ace.mqtt.exceptions.ASUnreachableException;
import com.ace.mqtt.exceptions.FailedAuthenticationException;
import com.ace.mqtt.utils.dataclasses.ClientRegistrationRequest;
import com.ace.mqtt.utils.dataclasses.ClientRegistrationResponse;
import com.ace.mqtt.utils.dataclasses.TokenRequest;
import com.ace.mqtt.utils.dataclasses.TokenRequestResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

class OauthHttpsClient {
    private final static Logger LOGGER = Logger.getLogger(OauthHttpsClient.class.getName());
    @NotNull private final EndpointRetriever endpointRetriever;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OauthHttpsClient(final String OauthServerAddress, final String OauthServerPort) {
        endpointRetriever = new EndpointRetriever("https", OauthServerAddress, OauthServerPort);
    }

    /**
     * @return SSLSocketFactory that trusts self signed certificates
     */
    private SSLSocketFactory getSocketFactory() {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustManagers = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(final X509Certificate[] certs, final String authType) {
                    }

                    public void checkServerTrusted(final X509Certificate[] certs, final String authType) {
                    }
                }
        };
        // Install the all-trusting trust manager
        final SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustManagers, new java.security.SecureRandom());
        } catch (final NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to set up SSL", e);
        }
        return sc.getSocketFactory();
    }

    /**
     * @param target          the target, including the desired endpoint, to connect to
     * @param method          REST method to be performed
     * @param allowSelfSigned allow connecting to servers with self signed certificates
     * @return https client pointing to the AS server token request endpoint
     * @throws ASUnreachableException if an I/O error happens
     */
    private HttpsURLConnection getHttpsClient(final String target, final String method, final boolean allowSelfSigned)
            throws ASUnreachableException {
        final URL url;
        try {
            url = new URL(target);
        } catch (final MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("AS server URL malformed", e);
        }
        final HttpsURLConnection con;
        try {
            con = (HttpsURLConnection) url.openConnection();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ASUnreachableException("Unable to contact AS server", e);
        }
        try {
            con.setRequestMethod(method);
        } catch (final ProtocolException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("GET method unsupported", e);
        }
        if (allowSelfSigned) {
            con.setHostnameVerifier(getInvalidHostnameVerifier());
            con.setSSLSocketFactory(getSocketFactory());
        }
        return con;
    }

    /**
     * @return a hostname verifier that allows invalid hostnames (for self signed certificates)
     */
    private HostnameVerifier getInvalidHostnameVerifier() {
        return (s, sslSession) -> true;
    }

    private int receive(final HttpsURLConnection con, final StringBuilder response) throws ASUnreachableException {
        final int responseCode;
        try (final BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            responseCode = con.getResponseCode();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ASUnreachableException("Unable to receive request response from AS server", e);
        }
        LOGGER.log(
                Level.FINE,
                String.format("Response:\t\nHeaders:\t%s\nBody:\t%s", con.getHeaderFields(), response.toString()));
        return responseCode;
    }

    private void send(final String body, final HttpsURLConnection con) throws ASUnreachableException {
        try (final OutputStreamWriter outputStream = new OutputStreamWriter(con.getOutputStream())) {
            outputStream.write(body);
            outputStream.flush();
        } catch (final IOException e) {
            e.printStackTrace();
            throw new ASUnreachableException("Unable to send request to AS server", e);
        }
    }

    @NotNull
    public TokenRequestResponse secureTokenRequest(
            @NotNull final byte[] authorizationHeader,
            @NotNull final TokenRequest tokenRequest
    ) throws ASUnreachableException, FailedAuthenticationException {
        final String stringifiedBody;
        try {
            stringifiedBody = new ObjectMapper().writeValueAsString(tokenRequest);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        final HttpsURLConnection con = getHttpsClient(
                endpointRetriever.getEndpoint(EndpointRetriever.ASEndpoint.TOKEN_REQUEST),
                "POST",
                true);
        con.setDoOutput(true);
        con.setDoInput(true);
        final String encodedAuth = Base64.getEncoder().encodeToString(authorizationHeader);
        con.setRequestProperty("Authorization", "Basic " + encodedAuth);
        con.setRequestProperty("Content-Type", "application/json");
        LOGGER.log(
                Level.FINE,
                String.format("Request:\t%s\nHeaders:\t%s\nBody:\t%s", con.toString(), con.getRequestProperties(),
                        stringifiedBody));
        send(stringifiedBody, con);
        final StringBuilder response = new StringBuilder();
        final int responseCode = receive(con, response);
        final String responseString = response.toString();
        con.disconnect();
        if ((int) (responseCode / 100) != 2) {
            // token request failed, invalid token
            throw new FailedAuthenticationException(responseString);
        }
        final TokenRequestResponse tokenRequestResponse;
        try {
            tokenRequestResponse = objectMapper.readValue(responseString, TokenRequestResponse.class);
        } catch (final JsonProcessingException e) {
            // Should never happen
            throw new IllegalArgumentException("Failed to parse POST response", e);
        }
        return tokenRequestResponse;
    }

    @NotNull
    public ClientRegistrationResponse registerClient(
            @NotNull final ClientRegistrationRequest requestBody
    ) throws ASUnreachableException, FailedAuthenticationException {
        final String stringifiedBody;
        try {
            stringifiedBody = new ObjectMapper().writeValueAsString(requestBody);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.getMessage());
        }
        final HttpsURLConnection con = getHttpsClient(
                endpointRetriever.getEndpoint(EndpointRetriever.ASEndpoint.CLIENT_REG),
                "POST",
                true);
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestProperty("Content-Type", "application/json");
        LOGGER.log(
                Level.FINE,
                String.format("Request:\t%s\nHeaders:\t%s\nBody:\t%s", con.toString(), con.getRequestProperties(),
                        stringifiedBody));
        send(stringifiedBody, con);
        final StringBuilder response = new StringBuilder();
        final int responseCode = receive(con, response);
        final String responseString = response.toString();
        con.disconnect();
        if ((int) (responseCode / 100) != 2) {
            // token request failed, invalid token
            throw new FailedAuthenticationException(responseString);
        }
        final ClientRegistrationResponse registrationResponse;
        try {
            registrationResponse = objectMapper.readValue(responseString, ClientRegistrationResponse.class);
        } catch (final JsonProcessingException e) {
            // Should never happen
            throw new IllegalArgumentException("Failed to parse POST response", e);
        }
        return registrationResponse;
    }
}
