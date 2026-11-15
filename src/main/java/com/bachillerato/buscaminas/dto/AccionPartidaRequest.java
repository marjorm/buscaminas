package com.bachillerato.buscaminas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccionPartidaRequest(
        @NotBlank String tipo,
        @NotNull @Min(0) Integer fila,
        @NotNull @Min(0) Integer columna
) { }
