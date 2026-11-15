package com.bachillerato.buscaminas.controllers;

import com.bachillerato.buscaminas.dto.AccionPartidaRequest;
import com.bachillerato.buscaminas.dto.CrearPartidaRequest;
import com.bachillerato.buscaminas.dto.PartidaJuegoResponse;
import com.bachillerato.buscaminas.game.NivelJuego;
import com.bachillerato.buscaminas.services.PartidaJuegoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class PartidaJuegoController {
    private final PartidaJuegoService servicio;

    public PartidaJuegoController(PartidaJuegoService servicio) {
        this.servicio = servicio;
    }

    private long usuario(Authentication autenticacion) {
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Inicia sesión");
        }
        try {
            return Long.parseLong(autenticacion.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida");
        }
    }

    @GetMapping("/api/niveles")
    public List<Map<String, Object>> niveles(Authentication autenticacion) {
        usuario(autenticacion);
        return Arrays.stream(NivelJuego.values())
                .map(n -> Map.<String, Object>of(
                        "id", n.name(), "nombre", n.name(), "filas", n.getFilas(),
                        "columnas", n.getColumnas(), "minas", n.getMinas()))
                .toList();
    }

    @PostMapping("/api/partidas")
    public ResponseEntity<PartidaJuegoResponse> crear(
            @Valid @RequestBody CrearPartidaRequest datos, Authentication autenticacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicio.crear(usuario(autenticacion), datos.nivel()));
    }

    @GetMapping("/api/partidas/actual")
    public PartidaJuegoResponse actual(Authentication autenticacion) {
        return servicio.actual(usuario(autenticacion));
    }

    @GetMapping("/api/partidas/{id}")
    public PartidaJuegoResponse terminada(@PathVariable long id, Authentication autenticacion) {
        return servicio.terminada(usuario(autenticacion), id);
    }

    @PostMapping("/api/partidas/{id}/acciones")
    public PartidaJuegoResponse accion(@PathVariable long id,
            @Valid @RequestBody AccionPartidaRequest datos, Authentication autenticacion) {
        return servicio.actuar(usuario(autenticacion), id, datos);
    }

    /** Operación adicional documentada: abandonar voluntariamente una partida activa. */
    @PostMapping("/api/partidas/{id}/abandonar")
    public PartidaJuegoResponse abandonar(@PathVariable long id, Authentication autenticacion) {
        return servicio.abandonar(usuario(autenticacion), id);
    }
}
