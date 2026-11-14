package com.bachillerato.buscaminas.repositories;

import com.bachillerato.buscaminas.models.Partida;
import com.bachillerato.buscaminas.models.EstadoPartida;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PartidaRepository extends JpaRepository<Partida, Long> {


    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.usuario.id = :usuarioId
            ORDER BY p.fechaInicio DESC
            """)
    List<Partida> obtenerHistorialPorUsuario(
            @Param("usuarioId") Long usuarioId
    );


    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.usuario.id = :usuarioId
            AND p.estado = :estado
            ORDER BY p.fechaInicio DESC
            """)
    List<Partida> buscarPartidasPorUsuarioYEstado(
            @Param("usuarioId") Long usuarioId,
            @Param("estado") EstadoPartida estado,
            Pageable limite
    );


    @Query("""
            SELECT p
            FROM Partida p
            WHERE p.id = :partidaId
            AND p.usuario.id = :usuarioId
            """)
    Optional<Partida> buscarPartidaDeUsuario(
            @Param("partidaId") Long partidaId,
            @Param("usuarioId") Long usuarioId
    );

    @Query(value = """
        SELECT
            usuario_id AS usuarioId,
            nombre_usuario AS nombreUsuario,
            email AS email,
            nivel AS nivel,
            mejor_tiempo_ms AS mejorTiempoMs,
            partidas_terminadas AS partidasTerminadas,
            ultimo_juego AS ultimoJuego,
            promedio_movimientos AS promedioMovimientos
        FROM vw_ranking
        ORDER BY mejor_tiempo_ms ASC
        """, nativeQuery = true)
    List<RankingProjection> obtenerRanking();
}