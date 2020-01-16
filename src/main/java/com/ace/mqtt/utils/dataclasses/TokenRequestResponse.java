package com.ace.mqtt.utils.dataclasses;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class TokenRequestResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String profile;
    @JsonProperty("token_type")
    private String tokenType;
    private long exp;
    private CNF cnf;

    public static class CNF {
        private JWK jwk;
        public JWK getJwk() {
            return jwk;
        }
    }

    public static class JWK {
        private String kty;
        private String alg;
        // key is base64 encoded
        private byte[] k;

        public String getKty() {
            return kty;
        }

        public String getAlg() {
            return alg;
        }

        public byte[] getK() {
            return k;
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getProfile() {
        return profile;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExp() {
        return exp;
    }

    public CNF getCnf() {
        return cnf;
    }

}
