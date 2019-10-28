package com.ace.mqtt.utils.dataclasses;

public final class TokenRequestResponse {
    private String access_token;
    private String profile;
    private String token_type;
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
        private String k;

        public String getKty() {
            return kty;
        }

        public String getAlg() {
            return alg;
        }

        public String getK() {
            return k;
        }
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getProfile() {
        return profile;
    }

    public String getToken_type() {
        return token_type;
    }

    public long getExp() {
        return exp;
    }

    public CNF getCnf() {
        return cnf;
    }

}
