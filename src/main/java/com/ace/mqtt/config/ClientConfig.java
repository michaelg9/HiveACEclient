package com.ace.mqtt.config;

import com.ace.mqtt.utils.dataclasses.ClientRegistrationResponse;
import io.reactivex.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Logger;

import static com.ace.mqtt.config.ConfigConstants.LOCAL_CONFIG_FILENAME;
import static com.ace.mqtt.config.ConfigConstants.PropertiesKeys.*;
import static com.ace.mqtt.utils.StringUtils.bytesToBase64;
import static com.ace.mqtt.utils.StringUtils.combineByteArrays;

public class ClientConfig implements Serializable {

    private final static Logger LOGGER = Logger.getLogger(ClientConfig.class.getName());
    private static ClientConfig instance = null;
    @NotNull
    final private Properties properties;
    @NotNull
    public final String rsServerIP;
    public final int rsServerPort;
    @Nullable
    public String asServerIP;
    public final String asServerPort;
    @Nullable
    public String clientID;
    @Nullable
    public byte[] clientSecret;
    @Nullable
    public final String clientUsername;
    @Nullable
    public final String clientUri;
    @NotNull
    public final char[] keyStorePass;
    @NotNull
    public final char[] trustStorePass;
    @NotNull
    public final String configDir;
    public final String grantType;
    public final String scope;
    public final String aud;

    private ClientConfig(@NonNull final Properties config) {
        this.properties = config;
        this.clientSecret =
                config.getProperty(CLIENT_SECRET) == null ? null : config.getProperty(CLIENT_SECRET).getBytes();
        this.clientID = config.getProperty(CLIENT_ID);
        this.clientUsername = config.getProperty(CLIENT_USERNAME);
        this.clientUri = config.getProperty(CLIENT_URI);
        this.rsServerIP = config.getProperty(RS_SERVER_IP);
        this.rsServerPort = Integer.parseInt(config.getProperty(RS_SERVER_PORT));
        this.asServerIP = config.getProperty(AS_SERVER_IP);
        this.asServerPort = config.getProperty(AS_SERVER_PORT);
        this.keyStorePass = config.getProperty(KEYSTORE_PASS).toCharArray();
        this.trustStorePass = config.getProperty(TRUSTSTORE_PASS).toCharArray();
        this.configDir = config.getProperty(CONFIG_DIR);
        this.grantType = "client_credentials";
        this.scope = "pub";
        this.aud = "humidity";
    }

    public Path getClientKeyFilename() {
        return Paths.get(configDir, "TLS", "mqtt-paho-client-1.jks");
    }

    public Path getClientTrustStoreFilename() {
        return Paths.get(configDir, "TLS", "mqtt-client-trust-store.jks");
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

    public void setAsServerIP(@NotNull final String asServerIP) {
        this.asServerIP = asServerIP;
        persist();
    }

    private void setClientID(final String clientID) {
        this.properties.setProperty(CLIENT_ID, clientID);
        this.clientID = clientID;
    }

    private void setClientSecret(@NotNull final byte[] clientSecret) {
        this.clientSecret = clientSecret;
        this.properties.setProperty(CLIENT_SECRET, bytesToBase64(clientSecret));
    }

    private boolean persist() {
        LOGGER.info("Persisting new properties");
        try (final OutputStream out = new FileOutputStream(Paths.get(configDir, LOCAL_CONFIG_FILENAME).toString())) {
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
        if (!persist()) {
            LOGGER.warning("Unable to save properties after obtaining registration credentials");
        }
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
            LOGGER.info("Loading client config from local properties file");
        } else {
            try (final InputStream input = new FileInputStream(configFilename)) {
                properties = new Properties();
                properties.load(input);
            } catch (final IOException e) {
                LOGGER.warning("Unable to read specified config file");
                e.printStackTrace();
                properties = null;
            }
        }
        if (properties == null) {
            throw new IllegalStateException("Client unable to be configured");
        }
        instance = new ClientConfig(properties);
        return instance;
    }
}
