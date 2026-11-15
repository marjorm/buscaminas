package com.bachillerato.buscaminas.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearPartidaRequest(@NotBlank String nivel) { }
