package com.ace.mqtt.utils.dataclasses;

public final class TokenRequestResponse {
    public String access_token;
    public String profile;
    public String token_type;
    public long exp;
    public CNF cnf;

    static class CNF {
        public JWK jwk;
    }

    static class JWK {
        public String kty;
        public String alg;
        public String k;
    }

}
