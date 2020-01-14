package com.ace.mqtt.utils;

import io.reactivex.annotations.NonNull;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Properties;

public class ClientConfig implements Serializable {
    private static ClientConfig instance = null;
    public final String rsServerIP;
    public final int rsServerPort;
    public final String asServerIP;
    public final String asServerPort;
    public final String clientID;
    public final byte[] clientSecret;
    public final char[] keyStorePass;
    public final char[] trustStorePass;
    public final String clientKeyFilename;
    public final String clientTrustStoreFilename;
    public final String grantType;
    public final String scope;
    public final String aud;
    public final byte[] secret;

    private ClientConfig(@NonNull final Properties config) {
        this.rsServerIP = config.getProperty("RSServerIP");
        this.rsServerPort = Integer.parseInt(config.getProperty("RSServerPort"));
        this.asServerIP = config.getProperty("ASServerIP");
        this.asServerPort = config.getProperty("ASServerPort");
        this.clientID = config.getProperty("ClientID");
        String clientSecret = config.getProperty("ClientSecret");
        this.clientSecret = clientSecret.getBytes();
        this.keyStorePass = config.getProperty("KeystorePass").toCharArray();
        this.trustStorePass = config.getProperty("KeystorePass").toCharArray();
        this.clientKeyFilename = config.getProperty("PrivateKeyStoreFilepath");
        this.clientTrustStoreFilename = config.getProperty("TrustStoreFilepath");
        this.grantType = "client_credentials";
        this.scope = "pub";
        this.aud = "humidity";
        this.secret = (this.clientID + ":" + clientSecret).getBytes();
        clientSecret = null;
    }

    public static ClientConfig getInstance(final String configFilename) throws IOException {
        if (instance != null) return instance;
        try (final InputStream input = new FileInputStream(configFilename)) {
            final Properties prop = new Properties();
            prop.load(input);
            instance = new ClientConfig(prop);
        } catch (final IOException e) {
            e.printStackTrace();
            throw e;
        }
        return instance;
    }
}
