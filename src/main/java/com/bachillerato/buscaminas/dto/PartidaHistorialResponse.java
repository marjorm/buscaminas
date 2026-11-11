
package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.models.Partida;

import java.time.LocalDateTime;

public record PartidaHistorialResponse(
        Long id,
        String nivel,
        String estado,
        Long tiempoMs,
        Integer movimientos,
        Short estrellas,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) {

    public static PartidaHistorialResponse desdePartida(Partida partida) {

        return new PartidaHistorialResponse(
                partida.getId(),
                partida.getNivel(),
                partida.getEstado().name(),
                partida.getTiempoMs(),
                partida.getMovimientos(),
                partida.getEstrellas(),
                partida.getFechaInicio(),
                partida.getFechaFin()
        );
    }
}
