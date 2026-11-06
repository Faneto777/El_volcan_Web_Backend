package com.elvolcan.backend.controller;

import com.elvolcan.backend.dto.AuthResponse;
import com.elvolcan.backend.dto.LoginRequest;
import com.elvolcan.backend.dto.RegistroRequest;
import com.elvolcan.backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro e inicio de sesion con JWT")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return usuarioService.login(req);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar cliente nuevo")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(req));
    }
}