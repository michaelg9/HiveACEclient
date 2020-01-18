package com.ace.mqtt.config;

final class ConfigConstants {
    static final String LOCAL_CONFIG_FILENAME = "local.properties";
    static class PropertiesKeys {
        static final String CLIENT_SECRET = "ClientSecret";
        static final String SECRET = "Secret";
        static final String CLIENT_ID = "ClientID";
        static final String CLIENT_USERNAME = "ClientUsername";
        static final String CLIENT_URI = "ClientUri";
        static final String RS_SERVER_IP = "RSServerIP";
        static final String RS_SERVER_PORT = "RSServerPort";
        static final String AS_SERVER_IP = "ASServerIP";
        static final String AS_SERVER_PORT = "ASServerPort";
        static final String KEYSTORE_PASS = "KeystorePass";
        static final String TRUSTSTORE_PASS = "TrustStorePass";
        static final String CONFIG_DIR = "ConfigDir";
    }
}
