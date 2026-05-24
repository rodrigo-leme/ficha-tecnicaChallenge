package com.automotiva.ficha_tecnica.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VehicleRequest(
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

        @NotEmpty(message = "Lista de atributos nao pode ser vazia")
        @Size(max = 30, message = "Quantidade de atributos excede o limite")
        List<
                @NotBlank(message = "Atributo nao pode ser vazio")
                @Size(max = 40, message = "Atributo excede o tamanho permitido")
                @Pattern(regexp = "^[a-z0-9_\\-]+$", message = "Atributo em formato invalido")
                String
                > atributos
) {}
