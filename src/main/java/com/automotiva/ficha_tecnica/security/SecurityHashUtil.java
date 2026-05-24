package com.automotiva.ficha_tecnica.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class SecurityHashUtil {

    private SecurityHashUtil() {
    }

    public static String pseudonymize(String value) {
        if (value == null || value.isBlank()) {
            return "anon";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return toHex(hash).substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            return "anon";
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
