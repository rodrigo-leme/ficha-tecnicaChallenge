package com.automotiva.ficha_tecnica.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Converter
public class SensitiveStringCryptoConverter implements AttributeConverter<String, String> {

    private static final String PREFIX = "ENC:";
    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int TAG_SIZE_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }

        if (attribute.startsWith(PREFIX)) {
            return attribute;
        }

        try {
            byte[] iv = new byte[IV_SIZE];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_SIZE_BITS, iv));
            byte[] encrypted = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);

            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao criptografar dado sensivel", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }

        if (!dbData.startsWith(PREFIX)) {
            return dbData;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(dbData.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(decoded);

            byte[] iv = new byte[IV_SIZE];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_SIZE_BITS, iv));
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao descriptografar dado sensivel", e);
        }
    }

    private SecretKeySpec getKey() {
        byte[] key = DataCryptoManager.getKeyBytes();
        if (key == null || key.length == 0) {
            throw new IllegalStateException("Chave de criptografia nao inicializada");
        }
        return new SecretKeySpec(key, AES);
    }
}
