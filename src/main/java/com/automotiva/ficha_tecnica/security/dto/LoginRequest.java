package com.automotiva.ficha_tecnica.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Usuario obrigatorio")
        @Size(max = 80, message = "Usuario invalido")
        String username,

        @NotBlank(message = "Senha obrigatoria")
        @Size(max = 80, message = "Senha invalida")
        String password
) {
}
