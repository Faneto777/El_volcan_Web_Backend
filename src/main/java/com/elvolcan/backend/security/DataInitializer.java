package com.elvolcan.backend.security;

import com.elvolcan.backend.model.Usuario;
import com.elvolcan.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearUsuarios(UsuarioRepository repo, PasswordEncoder encoder,
                                    @Value("${ADMIN_PASSWORD:}") String passAdmin,
                                    @Value("${VENDEDOR_PASSWORD:}") String passVendedor,
                                    @Value("${REPARTIDOR_PASSWORD:}") String passRepartidor) {
        return args -> {
            sembrar(repo, encoder, "Administrador", "admin@elvolcan.cl", passAdmin, "ADMIN");
            sembrar(repo, encoder, "Vendedor", "vendedor@elvolcan.cl", passVendedor, "VENDEDOR");
            sembrar(repo, encoder, "Repartidor", "repartidor@elvolcan.cl", passRepartidor, "REPARTIDOR");
        };
    }

    private void sembrar(UsuarioRepository repo, PasswordEncoder encoder,
                         String nombre, String email, String pass, String rol) {
        if (pass == null || pass.isBlank()) {
            return; // sin clave en secretos.properties: no se crea ni se cambia
        }
        Usuario u = repo.findByEmail(email).orElseGet(Usuario::new);
        if (u.getId() == null) {
            u.setNombre(nombre);
            u.setEmail(email);
            u.setRol(rol);
            u.setActivo(true);
        }
        u.setPassword(encoder.encode(pass));
        repo.save(u);
    }
}