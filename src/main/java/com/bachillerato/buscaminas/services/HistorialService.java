
package com.bachillerato.buscaminas.services;

import com.bachillerato.buscaminas.models.Partida;
import com.bachillerato.buscaminas.repositories.PartidaRepository;
import com.bachillerato.buscaminas.repositories.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HistorialService {

    private final PartidaRepository partidaRepository;
    private final UsuarioRepository usuarioRepository;

    public HistorialService(PartidaRepository partidaRepository, UsuarioRepository usuarioRepository) {

        this.partidaRepository = partidaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Partida> obtenerHistorial(Long usuarioId) {

        if (!usuarioRepository.existsById(usuarioId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "El usuario no existe"
            );
        }

        return partidaRepository.obtenerHistorialPorUsuario(usuarioId);
    }
}
