package com.automotiva.ficha_tecnica.security;

import com.automotiva.ficha_tecnica.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SecurityInputValidator {

    private static final Pattern SQLI_OR_COMMAND_PATTERN = Pattern.compile(
            "(?i)(<script|</script|\\bselect\\b.+\\bfrom\\b|\\binsert\\b.+\\binto\\b|\\bupdate\\b.+\\bset\\b|\\bdelete\\b.+\\bfrom\\b|\\bdrop\\b\\s+table|`|\\$\\(|\\|\\||&&|;\\s*--|/\\*|\\*/|\\brm\\b|\\bshutdown\\b)"
    );

    private static final Pattern SAFE_TEXT = Pattern.compile("^[\\p{L}0-9 .,_;:/()#%+\\-\"']+$");
    private static final Pattern SAFE_ATTRIBUTE = Pattern.compile("^[a-z0-9_\\-]+$");

    public String sanitizeText(String field, String value, int maxLen) {
        if (value == null) {
            throw new BadRequestException(field + " e obrigatorio");
        }

        String normalized = value.trim().replaceAll("\\s+", " ");

        if (normalized.isEmpty()) {
            throw new BadRequestException(field + " e obrigatorio");
        }

        if (normalized.length() > maxLen) {
            throw new BadRequestException(field + " excede o tamanho permitido");
        }

        if (SQLI_OR_COMMAND_PATTERN.matcher(normalized).find()) {
            throw new BadRequestException(field + " contem padrao malicioso");
        }

        if (!SAFE_TEXT.matcher(normalized).matches()) {
            throw new BadRequestException(field + " contem caracteres invalidos");
        }

        return normalized;
    }

    public String sanitizeAttributeName(String attribute) {
        if (attribute == null) {
            throw new BadRequestException("Atributo invalido");
        }

        String normalized = attribute.trim().toLowerCase();

        if (normalized.isEmpty() || normalized.length() > 40) {
            throw new BadRequestException("Atributo invalido");
        }

        if (!SAFE_ATTRIBUTE.matcher(normalized).matches()) {
            throw new BadRequestException("Atributo em formato invalido");
        }

        return normalized;
    }

    public String sanitizeSpecValue(String value) {
        if (value == null) {
            throw new BadRequestException("Valor do atributo e obrigatorio");
        }

        String normalized = value.trim().replaceAll("\\s+", " ");

        if (normalized.isEmpty()) {
            throw new BadRequestException("Valor do atributo e obrigatorio");
        }

        if (normalized.length() > 255) {
            throw new BadRequestException("Valor do atributo excede o tamanho permitido");
        }

        if (SQLI_OR_COMMAND_PATTERN.matcher(normalized).find()) {
            throw new BadRequestException("Valor do atributo contem padrao malicioso");
        }

        if (!SAFE_TEXT.matcher(normalized).matches()) {
            throw new BadRequestException("Valor do atributo contem caracteres invalidos");
        }

        return normalized;
    }
}
