package com.ace.mqtt.utils.dataclasses;

public final class TokenRequestResponse {
    public String access_token;
    public String profile;
    public String token_type;
    public long exp;
    public CNF cnf;

    public static class CNF {
        public JWK jwk;
    }

    public static class JWK {
        public String kty;
        public String alg;
        public String k;
    }

}
