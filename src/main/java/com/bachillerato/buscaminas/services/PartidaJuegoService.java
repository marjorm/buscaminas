package com.bachillerato.buscaminas.services;

import com.bachillerato.buscaminas.dto.AccionPartidaRequest;
import com.bachillerato.buscaminas.dto.PartidaJuegoResponse;
import com.bachillerato.buscaminas.game.MotorBuscaminas;
import com.bachillerato.buscaminas.game.NivelJuego;
import com.bachillerato.buscaminas.models.EstadoPartida;
import com.bachillerato.buscaminas.models.Partida;
import com.bachillerato.buscaminas.models.Usuario;
import com.bachillerato.buscaminas.repositories.PartidaRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class PartidaJuegoService {
    private final PartidaRepository partidas;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final MotorBuscaminas motor = new MotorBuscaminas();

    public PartidaJuegoService(PartidaRepository partidas, EntityManager entityManager,
                               ObjectMapper objectMapper) {
        this.partidas = partidas;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PartidaJuegoResponse crear(long usuarioId, String nivelSolicitado) {
        // Bloquear la fila del usuario evita dos partidas simultáneas con peticiones concurrentes.
        Usuario usuario = entityManager.find(Usuario.class, usuarioId, LockModeType.PESSIMISTIC_WRITE);
        if (usuario == null || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario no disponible");
        }
        if (!partidas.buscarPartidasPorUsuarioYEstado(usuarioId, EstadoPartida.EN_CURSO,
                PageRequest.of(0, 1)).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya tienes una partida en curso. Continúa o abandónala antes de crear otra.");
        }
        NivelJuego nivel;
        try {
            nivel = NivelJuego.valueOf(nivelSolicitado.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nivel inválido");
        }
        MotorBuscaminas.TableroPersistido tablero = motor.crear(nivel);
        Partida partida = new Partida();
        partida.setUsuario(usuario);
        partida.setNivel(nivel.name());
        partida.setEstado(EstadoPartida.EN_CURSO);
        partida.setMovimientos(0);
        partida.setEstrellas((short) 0);
        partida.setTablero(guardar(tablero));
        partida = partidas.saveAndFlush(partida);
        entityManager.refresh(partida);
        return respuesta(partida, tablero);
    }

    @Transactional(readOnly = true)
    public PartidaJuegoResponse actual(long usuarioId) {
        List<Partida> activas = partidas.buscarPartidasPorUsuarioYEstado(usuarioId,
                EstadoPartida.EN_CURSO, PageRequest.of(0, 1));
        if (activas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No tienes una partida en curso");
        }
        Partida partida = activas.get(0);
        return respuesta(partida, leer(partida));
    }

    @Transactional(readOnly = true)
    public PartidaJuegoResponse terminada(long usuarioId, long partidaId) {
        Partida partida = propia(usuarioId, partidaId);
        if (partida.getEstado() == EstadoPartida.EN_CURSO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Utiliza /api/partidas/actual para consultar la partida activa");
        }
        return respuesta(partida, leer(partida));
    }

    @Transactional
    public PartidaJuegoResponse actuar(long usuarioId, long partidaId, AccionPartidaRequest accion) {
        Partida partida = propia(usuarioId, partidaId);
        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La partida ya terminó");
        }
        MotorBuscaminas.TableroPersistido tablero = leer(partida);
        try {
            motor.actuar(tablero, accion.tipo().toUpperCase(Locale.ROOT),
                    accion.fila(), accion.columna());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        partida.setMovimientos(partida.getMovimientos() + 1);
        finalizarSiCorresponde(partida, tablero);
        partida.setTablero(guardar(tablero));
        partidas.saveAndFlush(partida);
        return respuesta(partida, tablero);
    }

    @Transactional
    public PartidaJuegoResponse abandonar(long usuarioId, long partidaId) {
        Partida partida = propia(usuarioId, partidaId);
        if (partida.getEstado() != EstadoPartida.EN_CURSO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La partida no está activa");
        }
        MotorBuscaminas.TableroPersistido tablero = leer(partida);
        tablero.resultado = "ABANDONADA";
        partida.setEstado(EstadoPartida.ABANDONADA);
        partida.setFechaFin(LocalDateTime.now());
        partida.setTiempoMs(duracion(partida, tablero));
        partida.setTablero(guardar(tablero));
        partidas.saveAndFlush(partida);
        return respuesta(partida, tablero);
    }

    private void finalizarSiCorresponde(Partida partida, MotorBuscaminas.TableroPersistido tablero) {
        if ("GANADA".equals(tablero.resultado) || "PERDIDA".equals(tablero.resultado)) {
            partida.setEstado(EstadoPartida.TERMINADA);
            partida.setFechaFin(LocalDateTime.now());
            partida.setTiempoMs(duracion(partida, tablero));
            partida.setEstrellas((short) ("GANADA".equals(tablero.resultado) ? 3 : 0));
        }
    }

    private long duracion(Partida partida, MotorBuscaminas.TableroPersistido tablero) {
        if (tablero.inicioEpochMs > 0L) {
            return Math.max(0L, System.currentTimeMillis() - tablero.inicioEpochMs);
        }
        return partida.getFechaInicio() == null ? 0L
                : Math.max(0L, Duration.between(partida.getFechaInicio(), LocalDateTime.now()).toMillis());
    }

    private Partida propia(long usuarioId, long partidaId) {
        Partida partida = partidas.findById(partidaId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
        if (!partida.getUsuario().getId().equals(usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puedes acceder a otra partida");
        }
        return partida;
    }

    private MotorBuscaminas.TableroPersistido leer(Partida partida) {
        try {
            var nodo = objectMapper.readTree(partida.getTablero());
            // Compatible con mapeos JPA JSON que guardan el contenido como cadena JSON.
            String json = nodo.isTextual() ? nodo.asText() : partida.getTablero();
            return objectMapper.readValue(json, MotorBuscaminas.TableroPersistido.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Tablero almacenado inválido en la partida " + partida.getId(), e);
        }
    }

    private String guardar(MotorBuscaminas.TableroPersistido tablero) {
        try {
            return objectMapper.writeValueAsString(tablero);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo persistir el tablero", e);
        }
    }

    private PartidaJuegoResponse respuesta(Partida partida, MotorBuscaminas.TableroPersistido tablero) {
        String resultado = tablero.resultado;
        if (resultado == null || resultado.isBlank() ||
                ("EN_CURSO".equals(resultado) && partida.getEstado() != EstadoPartida.EN_CURSO)) {
            resultado = partida.getEstado() == EstadoPartida.EN_CURSO ? "EN_CURSO" :
                    partida.getEstado() == EstadoPartida.ABANDONADA ? "ABANDONADA" :
                    partida.getEstrellas() != null && partida.getEstrellas() == 3 ? "GANADA" : "PERDIDA";
        }
        long duracion = partida.getTiempoMs() != null ? partida.getTiempoMs() : duracion(partida, tablero);
        return new PartidaJuegoResponse(
                partida.getId(), partida.getNivel(), partida.getEstado().name(), resultado,
                tablero.filas, tablero.columnas, tablero.num_minas, partida.getMovimientos(),
                tablero.banderas == null ? 0 : tablero.banderas.size(), duracion,
                partida.getFechaInicio(), partida.getFechaFin(), motor.vista(tablero));
    }
}
