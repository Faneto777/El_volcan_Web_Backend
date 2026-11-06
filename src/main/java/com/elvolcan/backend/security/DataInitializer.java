package com.elvolcan.backend.security;

import com.elvolcan.backend.model.Usuario;
import com.elvolcan.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearUsuarios(UsuarioRepository repo, PasswordEncoder encoder) {
        return args -> {
            crear(repo, encoder, "Administrador", "admin@elvolcan.cl", "Admin123!", "ADMIN");
            crear(repo, encoder, "Vendedor", "vendedor@elvolcan.cl", "Vendedor123!", "VENDEDOR");
        };
    }

    private void crear(UsuarioRepository repo, PasswordEncoder encoder,
                       String nombre, String email, String pass, String rol) {
        if (repo.findByEmail(email).isEmpty()) {
            Usuario u = new Usuario();
            u.setNombre(nombre);
            u.setEmail(email);
            u.setPassword(encoder.encode(pass));
            u.setRol(rol);
            u.setActivo(true);
            repo.save(u);
        }
    }
}