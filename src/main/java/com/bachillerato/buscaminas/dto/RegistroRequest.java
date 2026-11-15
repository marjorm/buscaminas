package com.bachillerato.buscaminas.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 80)
    private String nombre;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(max = 40)
    private String nombreUsuario;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo electrónico no es válido")
    @Size(max = 120)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*[0-9]).{8,}$",
            message = "La contraseña debe contener 8 caracteres, una letra y un número")
    private String password;

    @NotBlank(message = "Debes confirmar tu contraseña")
    private String confirmarPassword;

    @AssertTrue(message = "Las contraseñas no coinciden")
    public boolean isConfirmacionValida() {
        return password != null && password.equals(confirmarPassword);
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
}
