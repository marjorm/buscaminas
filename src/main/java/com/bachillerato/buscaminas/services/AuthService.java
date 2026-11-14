

package com.bachillerato.buscaminas.services;

import com.bachillerato.buscaminas.dto.LoginRequest;
import com.bachillerato.buscaminas.dto.RegistroRequest;
import com.bachillerato.buscaminas.models.Rol;
import com.bachillerato.buscaminas.models.Usuario;
import com.bachillerato.buscaminas.repositories.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordService passwordService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordService passwordService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordService = passwordService;
    }

    @Transactional
    public Usuario registrarUsuario(RegistroRequest request) {

        if (usuarioRepository.existsByNombreUsuario(request.getNombreUsuario())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El nombre de usuario ya está registrado"
            );
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El correo electrónico ya está registrado"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(request.getNombre());
        usuario.setNombreUsuario(request.getNombreUsuario());
        usuario.setEmail(request.getEmail());

        usuario.setPasswordHash(
                passwordService.generarHash(request.getPassword())
        );

        usuario.setRol(Rol.JUGADOR);
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario autenticarUsuario(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByNombreUsuario(request.getNombreUsuario())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Credenciales incorrectas"
                ));

        boolean passwordCorrecto = passwordService.verificarPassword(
                request.getPassword(),
                usuario.getPasswordHash()
        );

        if (!passwordCorrecto) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas"
            );
        }

        if (!usuario.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La cuenta está desactivada"
            );
        }

        return usuario;
    }


    @Transactional(readOnly = true)
    public Usuario obtenerUsuarioPorId(Long usuarioId) {

        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "El usuario de la sesión no existe"
                ));
    }

}
