package com.elvolcan.backend.service;

import com.elvolcan.backend.dto.AuthResponse;
import com.elvolcan.backend.dto.LoginRequest;
import com.elvolcan.backend.dto.RegistroRequest;
import com.elvolcan.backend.model.Usuario;
import com.elvolcan.backend.repository.UsuarioRepository;
import com.elvolcan.backend.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse registrar(RegistroRequest req) {
        if (usuarioRepository.findByEmail(req.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El email ya esta registrado");
        }
        Usuario u = new Usuario();
        u.setNombre(req.nombre());
        u.setEmail(req.email());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRol("CLIENTE"); // el registro publico siempre crea clientes
        u.setActivo(true);
        usuarioRepository.save(u);
        return respuesta(u);
    }

    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarioRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Credenciales invalidas"));
        if (!Boolean.TRUE.equals(u.getActivo())
                || !passwordEncoder.matches(req.password(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales invalidas");
        }
        return respuesta(u);
    }

    private AuthResponse respuesta(Usuario u) {
        String token = jwtUtil.generarToken(u.getEmail(), u.getRol());
        return new AuthResponse(token, u.getNombre(), u.getEmail(), u.getRol());
    }
}