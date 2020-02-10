package com.ace.mqtt.config;

final class ConfigConstants {
    static final String LOCAL_CONFIG_FILENAME = "local.properties";
    static final String DEFAULT_TCP_PORT = "1883";
    static final String DEFAULT_TLS_PORT = "8883";
    static final String DEFAULT_AS_PORT = "8001";
    static class PropertiesKeys {
        static final String CLIENT_SECRET = "ClientSecret";
        static final String CLIENT_ID = "ClientID";
        static final String CLIENT_USERNAME = "ClientUsername";
        static final String CLIENT_URI = "ClientUri";
        static final String RS_SERVER_IP = "RSServerIP";
        static final String RS_TLS_SERVER_PORT = "RSServerTLSPort";
        static final String RS_TCP_SERVER_PORT = "RSServerTCPPort";
        static final String AS_SERVER_IP = "ASServerIP";
        static final String AS_SERVER_PORT = "ASServerPort";
        static final String KEYSTORE_PASS = "KeystorePass";
        static final String TRUSTSTORE_PASS = "TrustStorePass";
        static final String CERT_DIR = "CertDir";
        static final String SCOPE = "Scope";
        static final String TOPIC = "Topic";
        static final String TRANSPORT = "Transport";
    }
}
