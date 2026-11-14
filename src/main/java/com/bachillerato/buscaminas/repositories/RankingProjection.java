package com.bachillerato.buscaminas.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RankingProjection {

    Long getUsuarioId();
    String getNombreUsuario();
    String getEmail();
    String getNivel();
    Long getMejorTiempoMs();
    Long getPartidasTerminadas();
    LocalDateTime getUltimoJuego();
    BigDecimal getPromedioMovimientos();
}