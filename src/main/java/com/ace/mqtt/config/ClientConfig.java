package com.ace.mqtt.config;

import com.ace.mqtt.utils.dataclasses.ClientRegistrationResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

import static com.ace.mqtt.config.ConfigConstants.*;
import static com.ace.mqtt.config.ConfigConstants.PropertiesKeys.*;
import static com.ace.mqtt.utils.StringUtils.bytesToBase64;
import static com.ace.mqtt.utils.StringUtils.combineByteArrays;

public class ClientConfig implements Serializable {

    public enum TRANSPORTTYPE {
        TLS,
        TCP,
        ACE;
    }

    private final static Logger LOGGER = Logger.getLogger(ClientConfig.class.getName());
    private static ClientConfig instance = null;
    @NotNull
    final private Properties properties;
    @NotNull
    public final String rsServerIP;
    public final int rsServerTLSPort;
    public final int rsServerTCPPort;
    @Nullable
    public String asServerIP;
    @Nullable
    public String asServerPort;
    @Nullable
    public String clientID;
    @Nullable
    public byte[] clientSecret;
    @Nullable
    public final String clientUsername;
    @Nullable
    public final String clientUri;
    @Nullable
    public final char[] keyStorePass;
    @Nullable
    public final char[] trustStorePass;
    @Nullable
    public final String certDir;
    public final String grantType;
    @NotNull
    private String scope;
    public final String aud;
    public final TRANSPORTTYPE transportType;

    private ClientConfig(@NotNull final Properties config) {
        this.properties = config;
        this.clientSecret =
                config.getProperty(CLIENT_SECRET) == null ? null : config.getProperty(CLIENT_SECRET).getBytes();
        this.clientID = config.getProperty(CLIENT_ID);
        this.clientUsername = config.getProperty(CLIENT_USERNAME);
        this.clientUri = config.getProperty(CLIENT_URI);
        this.rsServerIP = config.getProperty(RS_SERVER_IP);
        this.rsServerTLSPort = Integer.parseInt(config.getProperty(RS_TLS_SERVER_PORT, DEFAULT_TLS_PORT));
        this.rsServerTCPPort = Integer.parseInt(config.getProperty(RS_TCP_SERVER_PORT, DEFAULT_TCP_PORT));
        this.asServerIP = config.getProperty(AS_SERVER_IP);
        this.asServerPort = config.getProperty(AS_SERVER_PORT, DEFAULT_AS_PORT);
        this.keyStorePass = config.containsKey(KEYSTORE_PASS) ? config.getProperty(KEYSTORE_PASS).toCharArray() : null;
        this.trustStorePass =
                config.containsKey(TRUSTSTORE_PASS) ? config.getProperty(TRUSTSTORE_PASS).toCharArray() : null;
        this.certDir = config.getProperty(CERT_DIR);
        this.grantType = "client_credentials";
        this.scope = config.getProperty(SCOPE);
        this.aud = config.getProperty(TOPIC);
        String type = config.getProperty(TRANSPORT, TRANSPORTTYPE.TLS.name()).toUpperCase();
        try {
            TRANSPORTTYPE.valueOf(type);
        } catch (final IllegalArgumentException e) {
            LOGGER.warning("Illegal transport type found in config file: " + type);
            type = TRANSPORTTYPE.TLS.name();
        }
        this.transportType = TRANSPORTTYPE.valueOf(type);
        LOGGER.fine("Transport type set to: " + this.transportType.name());
        if (this.transportType.equals(TRANSPORTTYPE.TLS) &&
                (this.certDir == null || this.keyStorePass == null || this.trustStorePass == null)) {
            throw new IllegalStateException("Incomplete info found for TLS transport");
        }
    }

    public Path getClientKeyFilename() {
        return Paths.get(certDir, "client-key.jks");
    }

    public Path getClientTrustStoreFilename() {
        return Paths.get(certDir, "client-truststore.jks");
    }

    public byte[] getHTTPAuthSecret() {
        return clientID != null && clientSecret != null ?
                combineByteArrays((this.clientID + ":").getBytes(), clientSecret) : null;
    }

    public boolean isClientRegistered() {
        return this.getHTTPAuthSecret() != null;
    }

    public boolean canClientRegister() {
        return !(this.clientUsername == null || this.clientUri == null);
    }

    public boolean isASInfoAvailable() {
        return this.asServerIP != null && this.asServerPort != null;
    }

    public void setAsServerIP(final String asServerIP) {
        this.asServerIP = asServerIP;
        this.properties.setProperty(AS_SERVER_IP, asServerIP);
    }

    public void setAsServerPort(final String asServerPort) {
        this.asServerPort = asServerPort;
        this.properties.setProperty(AS_SERVER_PORT, asServerPort);
    }

    private void setClientID(final String clientID) {
        this.clientID = clientID;
        this.properties.setProperty(CLIENT_ID, clientID);
    }

    private void setClientSecret(@NotNull final byte[] clientSecret) {
        this.clientSecret = clientSecret;
        this.properties.setProperty(CLIENT_SECRET, bytesToBase64(clientSecret));
    }

    @NotNull
    public String getScope() {
        return scope;
    }

    public void setScope(@NotNull final String scope) {
        this.scope = scope;
    }

    public boolean persist(@NotNull final String dir) {
        LOGGER.fine("Persisting new properties");
        try (final OutputStream out = new FileOutputStream(Paths.get(dir, LOCAL_CONFIG_FILENAME).toString())) {
            properties.store(out, "---No comments---");
        } catch (final IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void registerClient(@NotNull final ClientRegistrationResponse response) {
        if (this.isClientRegistered()) {
            throw new IllegalStateException("Client can't register twice");
        }
        this.setClientID(response.getClientID());
        this.setClientSecret(response.getClientSecret());
    }

    @Nullable
    private static URL getLocalConfig() {
        return ClientConfig.class.getResource(LOCAL_CONFIG_FILENAME);
    }

    @Nullable
    private static Properties readLocalConfig() {
        final URL url = getLocalConfig();
        Properties prop = null;
        if (url != null) {
            try (final InputStream stream = url.openStream()) {
                prop = new Properties();
                prop.load(stream);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
        return prop;
    }

    public static ClientConfig getInstance(@NotNull final String configFilename) {
        return getInstance(configFilename, false);
    }

    /**
     * Reads and returns the client config. If it's the client's first run (or you'd like to reset the client config)
     * then there will be no local config file and an external file is required. If it's not the client's first run,
     * then the client should have registered with an AS, obtained client id and secret and should have persisted them
     * in a local file.
     *
     * @param configFilename Full path of an external client config file in properties format
     * @param preferLocal    if a local config file is found, prefer it over the external
     * @return a client config object if either the external or the local config was successfully fetched
     */
    public static ClientConfig getInstance(@NotNull final String configFilename, final boolean preferLocal) {
        if (instance != null) {
            return instance;
        }
        Properties properties = null;
        if (preferLocal) {
            properties = readLocalConfig();
        }
        if (properties != null) {
            instance = new ClientConfig(properties);
            LOGGER.fine("Loading client config from local properties file");
        } else {
            try (final InputStream input = new FileInputStream(configFilename)) {
                properties = new Properties();
                properties.load(input);
            } catch (final IOException e) {
                LOGGER.warning("Unable to read specified config file " + configFilename);
                throw new IllegalStateException("Client unable to be configured", e);
            }
        }
        instance = new ClientConfig(properties);
        return instance;
    }
}
