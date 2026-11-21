package com.elvolcan.backend.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> manejarRespuesta(
            ResponseStatusException exception) {
        HttpStatusCode estado = exception.getStatusCode();
        return ResponseEntity.status(estado).body(Map.of(
                "status", estado.value(),
                "error", exception.getReason() != null
                        ? exception.getReason()
                        : "Error en la solicitud"));
    }
}
