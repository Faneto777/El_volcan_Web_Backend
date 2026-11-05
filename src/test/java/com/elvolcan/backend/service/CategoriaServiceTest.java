package com.elvolcan.backend.service;

import com.elvolcan.backend.model.Categoria;
import com.elvolcan.backend.repository.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository);
    }

    @Test
    void deberiaCrearCategoriaSinConservarIdRecibido() {
        Categoria categoria = new Categoria();
        categoria.setId(99L);
        categoria.setNombre("Cilindros");

        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        categoriaService.crear(categoria);

        assertEquals(null, categoria.getId());
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void deberiaRechazarCategoriaSinNombre() {
        Categoria categoria = new Categoria();
        categoria.setNombre(" ");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.crear(categoria));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarCategoriaInexistenteAlConsultar() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.obtenerPorId(99L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
