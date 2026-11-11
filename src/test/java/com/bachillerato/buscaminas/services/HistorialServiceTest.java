
package com.bachillerato.buscaminas.services;

import com.bachillerato.buscaminas.models.Partida;
import com.bachillerato.buscaminas.repositories.PartidaRepository;
import com.bachillerato.buscaminas.repositories.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistorialServiceTest {

    private PartidaRepository partidaRepository;
    private UsuarioRepository usuarioRepository;
    private HistorialService historialService;

    @BeforeEach
    void configurar() {
        partidaRepository = mock(PartidaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);

        historialService = new HistorialService(
                partidaRepository,
                usuarioRepository
        );
    }

    @Test
    void obtenerHistorialDeUsuarioExistente() {
        Partida partida = new Partida();
        partida.setId(2L);
        partida.setNivel("PRINCIPIANTE");

        when(usuarioRepository.existsById(3L))
                .thenReturn(true);

        when(partidaRepository.obtenerHistorialPorUsuario(3L))
                .thenReturn(List.of(partida));

        List<Partida> resultado =
                historialService.obtenerHistorial(3L);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getId());
        assertEquals("PRINCIPIANTE", resultado.get(0).getNivel());

        verify(partidaRepository).obtenerHistorialPorUsuario(3L);
    }

    @Test
    void obtenerHistorialDeUsuarioInexistente() {
        when(usuarioRepository.existsById(999L))
                .thenReturn(false);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> historialService.obtenerHistorial(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        verify(partidaRepository, never())
                .obtenerHistorialPorUsuario(anyLong());
    }

    @Test
    void obtenerHistorialVacio() {
        when(usuarioRepository.existsById(3L))
                .thenReturn(true);

        when(partidaRepository.obtenerHistorialPorUsuario(3L))
                .thenReturn(List.of());

        List<Partida> resultado =
                historialService.obtenerHistorial(3L);

        assertTrue(resultado.isEmpty());
    }
}
