package com.automotiva.ficha_tecnica.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private boolean requireHttps = true;
    private List<String> allowedOrigins = new ArrayList<>();

    private String jwtSecret;
    private long jwtExpirationSeconds = 900;
    private long jwtRefreshExpirationSeconds = 86400;
    private String jwtIssuer = "ficha-tecnica-api";

    private boolean payloadSignatureEnabled = true;
    private String payloadSignatureSecret;

    private int rateLimitRequestsPerMinute = 120;

    private String dataEncryptionKey;
    private int dataRetentionDays = 90;

    private DefaultUsers defaultUsers = new DefaultUsers();

    public boolean isRequireHttps() {
        return requireHttps;
    }

    public void setRequireHttps(boolean requireHttps) {
        this.requireHttps = requireHttps;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpirationSeconds() {
        return jwtExpirationSeconds;
    }

    public void setJwtExpirationSeconds(long jwtExpirationSeconds) {
        this.jwtExpirationSeconds = jwtExpirationSeconds;
    }

    public long getJwtRefreshExpirationSeconds() {
        return jwtRefreshExpirationSeconds;
    }

    public void setJwtRefreshExpirationSeconds(long jwtRefreshExpirationSeconds) {
        this.jwtRefreshExpirationSeconds = jwtRefreshExpirationSeconds;
    }

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public boolean isPayloadSignatureEnabled() {
        return payloadSignatureEnabled;
    }

    public void setPayloadSignatureEnabled(boolean payloadSignatureEnabled) {
        this.payloadSignatureEnabled = payloadSignatureEnabled;
    }

    public String getPayloadSignatureSecret() {
        return payloadSignatureSecret;
    }

    public void setPayloadSignatureSecret(String payloadSignatureSecret) {
        this.payloadSignatureSecret = payloadSignatureSecret;
    }

    public int getRateLimitRequestsPerMinute() {
        return rateLimitRequestsPerMinute;
    }

    public void setRateLimitRequestsPerMinute(int rateLimitRequestsPerMinute) {
        this.rateLimitRequestsPerMinute = rateLimitRequestsPerMinute;
    }

    public String getDataEncryptionKey() {
        return dataEncryptionKey;
    }

    public void setDataEncryptionKey(String dataEncryptionKey) {
        this.dataEncryptionKey = dataEncryptionKey;
    }

    public int getDataRetentionDays() {
        return dataRetentionDays;
    }

    public void setDataRetentionDays(int dataRetentionDays) {
        this.dataRetentionDays = dataRetentionDays;
    }

    public DefaultUsers getDefaultUsers() {
        return defaultUsers;
    }

    public void setDefaultUsers(DefaultUsers defaultUsers) {
        this.defaultUsers = defaultUsers;
    }

    public static class DefaultUsers {
        private UserCredential admin = new UserCredential("admin", "admin123");
        private UserCredential analyst = new UserCredential("analista", "analista123");
        private UserCredential user = new UserCredential("usuario", "usuario123");

        public UserCredential getAdmin() {
            return admin;
        }

        public void setAdmin(UserCredential admin) {
            this.admin = admin;
        }

        public UserCredential getAnalyst() {
            return analyst;
        }

        public void setAnalyst(UserCredential analyst) {
            this.analyst = analyst;
        }

        public UserCredential getUser() {
            return user;
        }

        public void setUser(UserCredential user) {
            this.user = user;
        }
    }

    public static class UserCredential {
        private String username;
        private String password;

        public UserCredential() {
        }

        public UserCredential(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
