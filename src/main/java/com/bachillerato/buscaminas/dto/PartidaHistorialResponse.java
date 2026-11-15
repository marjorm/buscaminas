package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.models.Partida;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

public record PartidaHistorialResponse(
        Long id,
        String nivel,
        String estado,
        String resultado,
        Long tiempoMs,
        Integer movimientos,
        Short estrellas,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {
    private static final ObjectMapper JSON = new ObjectMapper();

    public static PartidaHistorialResponse desdePartida(Partida partida) {
        String resultado = partida.getEstado().name();
        try {
            JsonNode raiz = JSON.readTree(partida.getTablero());
            if (raiz.isTextual()) raiz = JSON.readTree(raiz.asText());
            String almacenado = raiz.path("resultado").asText("");
            if (!almacenado.isBlank()) {
                resultado = almacenado;
            } else if ("TERMINADA".equals(resultado)) {
                resultado = partida.getEstrellas() != null && partida.getEstrellas() == 3
                        ? "GANADA" : "PERDIDA";
            }
        } catch (Exception ignorada) {
            // Conserva estado aunque exista una partida legada no compatible.
        }
        return new PartidaHistorialResponse(partida.getId(), partida.getNivel(),
                partida.getEstado().name(), resultado, partida.getTiempoMs(),
                partida.getMovimientos(), partida.getEstrellas(), partida.getFechaInicio(),
                partida.getFechaFin());
    }
}
