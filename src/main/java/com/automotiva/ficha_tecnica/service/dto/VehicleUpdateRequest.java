package com.automotiva.ficha_tecnica.service.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record VehicleUpdateRequest(
        @Size(max = 60, message = "Marca excede o tamanho permitido")
        @Pattern(regexp = "^[\\p{L}0-9 ._\\-]+$", message = "Marca em formato invalido")
        String marca,

        @Size(max = 60, message = "Modelo excede o tamanho permitido")
        @Pattern(regexp = "^[\\p{L}0-9 ._\\-]+$", message = "Modelo em formato invalido")
        String modelo,

        @Size(max = 60, message = "Versao excede o tamanho permitido")
        @Pattern(regexp = "^[\\p{L}0-9 ._\\-]+$", message = "Versao em formato invalido")
        String versao,

        @Size(max = 40, message = "Quantidade de especificacoes excede o limite")
        Map<
                @Size(max = 40, message = "Atributo excede o tamanho permitido")
                @Pattern(regexp = "^[a-z0-9_\\-]+$", message = "Atributo em formato invalido")
                String,

                @Size(max = 255, message = "Valor do atributo excede o tamanho permitido")
                @Pattern(regexp = "^[\\p{L}0-9 .,_;:/()#%+\\-\"']+$", message = "Valor do atributo em formato invalido")
                String
                > especificacoes
) {
}
