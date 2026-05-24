package com.automotiva.ficha_tecnica.security.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token obrigatorio")
        String refreshToken
) {
}
