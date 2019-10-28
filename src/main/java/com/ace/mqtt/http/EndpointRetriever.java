package com.ace.mqtt.http;

import org.jetbrains.annotations.NotNull;

class EndpointRetriever {
    private final String protocol;
    private final String target;
    private final String port;

    EndpointRetriever(@NotNull final String protocol, @NotNull final String target, @NotNull final String port) {
        this.protocol = protocol;
        this.target = target;
        this.port = port;
    }

    @NotNull String getEndpoint(@NotNull final EndpointRetriever.ASEndpoint endpoint) {
        return String.format("%s://%s:%s%s", protocol, target, port, endpoint.name);
    }

    public enum ASEndpoint {
        TOKEN_REQUEST("/api/client/token");
        private final String name;
        ASEndpoint(@NotNull final String name) {
            this.name = name;
        }
    }
}
