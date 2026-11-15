package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.repositories.RankingProjection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Datos que necesita el ranking. El correo de otros jugadores no se publica. */
public record RankingResponse(
        Long usuarioId,
        String nombreUsuario,
        String nivel,
        Long mejorTiempoMs,
        Long partidasTerminadas,
        LocalDateTime ultimoJuego,
        BigDecimal promedioMovimientos
) {
    public static RankingResponse desde(RankingProjection p) {
        return new RankingResponse(p.getUsuarioId(), p.getNombreUsuario(),
                p.getNivel(), p.getMejorTiempoMs(), p.getPartidasTerminadas(),
                p.getUltimoJuego(), p.getPromedioMovimientos());
    }
}
