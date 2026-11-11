
package com.bachillerato.buscaminas.services;

import com.bachillerato.buscaminas.dto.LoginRequest;
import com.bachillerato.buscaminas.models.Rol;
import com.bachillerato.buscaminas.models.Usuario;
import com.bachillerato.buscaminas.repositories.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.bachillerato.buscaminas.dto.RegistroRequest;

class AuthServiceTest {

    private UsuarioRepository usuarioRepository;
    private PasswordService passwordService;
    private AuthService authService;

    @BeforeEach
    void configurar() {
        usuarioRepository = mock(UsuarioRepository.class);
        passwordService = mock(PasswordService.class);

        authService = new AuthService(
                usuarioRepository,
                passwordService
        );
    }

    @Test
    void loginConCredencialesCorrectas() {
        Usuario usuario = new Usuario();
        usuario.setId(3L);
        usuario.setNombreUsuario("prueba_buscaminas");
        usuario.setPasswordHash("hash_de_prueba");
        usuario.setRol(Rol.JUGADOR);
        usuario.setActivo(true);

        LoginRequest request = new LoginRequest();
        request.setNombreUsuario("prueba_buscaminas");
        request.setPassword("Clave12345");

        when(usuarioRepository.findByNombreUsuario("prueba_buscaminas"))
                .thenReturn(Optional.of(usuario));

        when(passwordService.verificarPassword(
                "Clave12345",
                "hash_de_prueba"
        )).thenReturn(true);

        Usuario resultado = authService.autenticarUsuario(request);

        assertEquals(3L, resultado.getId());
        assertEquals("prueba_buscaminas", resultado.getNombreUsuario());
    }

    @Test
    void loginConPasswordIncorrecta() {
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario("prueba_buscaminas");
        usuario.setPasswordHash("hash_de_prueba");
        usuario.setActivo(true);

        LoginRequest request = new LoginRequest();
        request.setNombreUsuario("prueba_buscaminas");
        request.setPassword("Incorrecta");

        when(usuarioRepository.findByNombreUsuario("prueba_buscaminas"))
                .thenReturn(Optional.of(usuario));

        when(passwordService.verificarPassword(
                "Incorrecta",
                "hash_de_prueba"
        )).thenReturn(false);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.autenticarUsuario(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    @Test
    void loginConUsuarioInexistente() {
        LoginRequest request = new LoginRequest();
        request.setNombreUsuario("no_existe");
        request.setPassword("Clave12345");

        when(usuarioRepository.findByNombreUsuario("no_existe"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.autenticarUsuario(request)
        );

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }


    @Test
    void registroConNombreUsuarioDuplicado() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Jugador Prueba");
        request.setNombreUsuario("prueba_buscaminas");
        request.setEmail("nuevo@ejemplo.com");
        request.setPassword("Clave12345");

        when(usuarioRepository.existsByNombreUsuario("prueba_buscaminas"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.registrarUsuario(request)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void registroConCorreoDuplicado() {
        RegistroRequest request = new RegistroRequest();
        request.setNombre("Jugador Nuevo");
        request.setNombreUsuario("jugador_nuevo");
        request.setEmail("prueba_buscaminas@ejemplo.com");
        request.setPassword("Clave12345");

        when(usuarioRepository.existsByNombreUsuario("jugador_nuevo"))
                .thenReturn(false);

        when(usuarioRepository.existsByEmail(
                "prueba_buscaminas@ejemplo.com"
        )).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> authService.registrarUsuario(request)
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

}
