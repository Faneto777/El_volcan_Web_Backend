package com.elvolcan.backend.service;

import com.elvolcan.backend.model.Categoria;
import com.elvolcan.backend.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    }

    public Categoria crear(Categoria categoria) {
        validarCategoria(categoria);
        categoria.setId(null);
        return categoriaRepository.save(categoria);
    }

    public Categoria actualizar(Long id, Categoria datosActualizados) {
        validarCategoria(datosActualizados);

        Categoria categoria = obtenerPorId(id);
        categoria.setNombre(datosActualizados.getNombre());
        categoria.setDescripcion(datosActualizados.getDescripcion());
        return categoriaRepository.save(categoria);
    }

    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);
        categoriaRepository.delete(categoria);
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null || categoria.getNombre() == null
                || categoria.getNombre().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El nombre de la categoría es obligatorio");
        }
    }
}
