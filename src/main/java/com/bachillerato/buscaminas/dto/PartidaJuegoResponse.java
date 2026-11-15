package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.game.MotorBuscaminas.CasillaVisible;
import java.time.LocalDateTime;
import java.util.List;

public record PartidaJuegoResponse(
        Long id,
        String nivel,
        String estado,
        String resultado,
        int filas,
        int columnas,
        int minas,
        int movimientos,
        int banderas,
        long tiempoMs,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        List<List<CasillaVisible>> casillas
) { }
