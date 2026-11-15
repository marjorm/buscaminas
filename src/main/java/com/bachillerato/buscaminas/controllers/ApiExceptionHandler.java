package com.bachillerato.buscaminas.controllers;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public record ErrorApi(int codigo, String mensaje) { }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorApi> estado(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode())
                .body(new ErrorApi(error.getStatusCode().value(),
                        error.getReason() == null ? "Solicitud no permitida" : error.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> validacion(MethodArgumentNotValidException error) {
        String mensaje = error.getBindingResult().getAllErrors().stream()
                .findFirst().map(e -> e.getDefaultMessage()).orElse("Datos inválidos");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler({ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorApi> solicitudInvalida(Exception error) {
        return respuesta(HttpStatus.BAD_REQUEST, "Solicitud inválida");
    }

    @ExceptionHandler({DataIntegrityViolationException.class,
            OptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorApi> conflicto(Exception error) {
        return respuesta(HttpStatus.CONFLICT, "Conflicto de datos. Actualiza la partida e inténtalo de nuevo.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> inesperado(Exception error) {
        // Preservar el estado HTTP de errores de infraestructura (404, 405, 415, etc.).
        if (error instanceof ErrorResponse respuestaHttp) {
            int codigo = respuestaHttp.getStatusCode().value();
            return ResponseEntity.status(respuestaHttp.getStatusCode())
                    .body(new ErrorApi(codigo, "Solicitud no permitida"));
        }
        LOG.error("Error inesperado durante una operación API", error);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "No fue posible completar la solicitud");
    }

    private ResponseEntity<ErrorApi> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorApi(estado.value(), mensaje));
    }
}
