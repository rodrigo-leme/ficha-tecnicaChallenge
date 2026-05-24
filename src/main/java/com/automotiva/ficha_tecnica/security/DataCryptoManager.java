package com.automotiva.ficha_tecnica.security;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

@Component
public class DataCryptoManager {

    private static byte[] keyBytes;

    private final SecurityProperties securityProperties;

    public DataCryptoManager(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    public void init() {
        keyBytes = hashToKey(securityProperties.getDataEncryptionKey());
    }

    public static byte[] getKeyBytes() {
        return keyBytes;
    }

    private byte[] hashToKey(String source) {
        if (source == null || source.isBlank()) {
            throw new IllegalStateException("app.security.data-encryption-key nao configurada");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(source.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, 32);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Falha ao preparar chave de criptografia", e);
        }
    }
}
