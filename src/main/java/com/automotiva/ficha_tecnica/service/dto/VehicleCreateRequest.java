package com.automotiva.ficha_tecnica.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record VehicleCreateRequest(
        @NotBlank(message = "Marca e obrigatoria")
        @Size(max = 60, message = "Marca excede o tamanho permitido")
        @Pattern(regexp = "^[\\p{L}0-9 ._\\-]+$", message = "Marca em formato invalido")
        String marca,

        @NotBlank(message = "Modelo e obrigatorio")
        @Size(max = 60, message = "Modelo excede o tamanho permitido")
        @Pattern(regexp = "^[\\p{L}0-9 ._\\-]+$", message = "Modelo em formato invalido")
        String modelo,

        @NotBlank(message = "Versao e obrigatoria")
        @Size(max = 60, message = "Versao excede o tamanho permitido")
        @Pattern(regexp = "^[\\p{L}0-9 ._\\-]+$", message = "Versao em formato invalido")
        String versao,

        @NotEmpty(message = "Especificacoes nao podem ser vazias")
        @Size(max = 40, message = "Quantidade de especificacoes excede o limite")
        Map<
                @NotBlank(message = "Nome do atributo invalido")
                @Size(max = 40, message = "Atributo excede o tamanho permitido")
                @Pattern(regexp = "^[a-z0-9_\\-]+$", message = "Atributo em formato invalido")
                String,

                @NotBlank(message = "Valor do atributo invalido")
                @Size(max = 255, message = "Valor do atributo excede o tamanho permitido")
                @Pattern(regexp = "^[\\p{L}0-9 .,_;:/()#%+\\-\"']+$", message = "Valor do atributo em formato invalido")
                String
                > especificacoes) {
}
