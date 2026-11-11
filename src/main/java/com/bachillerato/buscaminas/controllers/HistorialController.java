
package com.bachillerato.buscaminas.controllers;

import com.bachillerato.buscaminas.dto.PartidaHistorialResponse;
import com.bachillerato.buscaminas.models.Partida;
import com.bachillerato.buscaminas.services.HistorialService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @GetMapping("/{id}/partidas")
    public ResponseEntity<List<PartidaHistorialResponse>> obtenerHistorial(@PathVariable Long id, Authentication autenticacion) {

        Long usuarioAutenticado;

        try {
            usuarioAutenticado = Long.valueOf(autenticacion.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "La sesión no es válida"
            );
        }

        boolean esAdministrador = autenticacion.getAuthorities()
                .stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMIN"));

        if (!usuarioAutenticado.equals(id) && !esAdministrador) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes consultar las partidas de otro usuario"
            );
        }

        List<Partida> partidas = historialService.obtenerHistorial(id);

        List<PartidaHistorialResponse> respuesta = partidas
                .stream()
                .map(PartidaHistorialResponse::desdePartida)
                .toList();

        return ResponseEntity.ok(respuesta);
    }
}
