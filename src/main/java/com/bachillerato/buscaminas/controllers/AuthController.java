
package com.bachillerato.buscaminas.controllers;

import com.bachillerato.buscaminas.dto.LoginRequest;
import com.bachillerato.buscaminas.dto.RegistroRequest;
import com.bachillerato.buscaminas.dto.UsuarioResponse;
import com.bachillerato.buscaminas.models.Usuario;
import com.bachillerato.buscaminas.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegistroRequest request) {

        Usuario usuario = authService.registrarUsuario(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponse.desdeUsuario(usuario));
    }


    @PostMapping("/login")
    public ResponseEntity<UsuarioResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        Usuario usuario = authService.autenticarUsuario(request);

        HttpSession sesionAnterior = httpRequest.getSession(false);

        if (sesionAnterior != null) {
            sesionAnterior.invalidate();
        }

        HttpSession sesion = httpRequest.getSession(true);

        Authentication autenticacion = new UsernamePasswordAuthenticationToken(
                        usuario.getId().toString(),
                        null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_" + usuario.getRol().name()
                        ))
                );

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacion);
        SecurityContextHolder.setContext(contexto);

        sesion.setAttribute("usuarioId", usuario.getId());

        sesion.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                contexto
        );

        return ResponseEntity.ok(
                UsuarioResponse.desdeUsuario(usuario)
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {

        HttpSession sesion = request.getSession(false);
        SecurityContextHolder.clearContext();

        if (sesion != null) {
            sesion.invalidate();
        }

        return ResponseEntity.noContent().build();
    }


    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me(HttpServletRequest request) {

        HttpSession sesion = request.getSession(false);

        if (sesion == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No hay una sesión activa"
            );
        }

        Long usuarioId = (Long) sesion.getAttribute("usuarioId");

        if (usuarioId == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "No hay un usuario autenticado"
            );
        }

        Usuario usuario = authService.obtenerUsuarioPorId(usuarioId);

        if (!usuario.getActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La cuenta está desactivada"
            );
        }

        return ResponseEntity.ok(
                UsuarioResponse.desdeUsuario(usuario)
        );
    }

}
