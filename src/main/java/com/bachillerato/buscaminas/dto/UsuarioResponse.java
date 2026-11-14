
package com.bachillerato.buscaminas.dto;

import com.bachillerato.buscaminas.models.Usuario;

public record UsuarioResponse(
        Long id,
        String nombre,
        String nombreUsuario,
        String email,
        String rol
) {
    public static UsuarioResponse desdeUsuario(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getNombreUsuario(),
                usuario.getEmail(),
                usuario.getRol().name()
        );
    }
}
