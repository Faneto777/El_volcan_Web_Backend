package com.elvolcan.backend.service;

import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.model.Categoria;
import com.elvolcan.backend.repository.CategoriaRepository;
import com.elvolcan.backend.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(String id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public List<Producto> obtenerPorCategoria(Long categoriaId) {
        verificarCategoria(categoriaId);
        return productoRepository.findByCategoriaId(categoriaId);
    }

    public List<Producto> obtenerOfertas() {
        return productoRepository.findByOfertaTrue();
    }

    public Producto crear(Producto producto) {
        validarProducto(producto, true);
        if (productoRepository.existsById(producto.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Ya existe un producto con ese ID");
        }
        producto.setCategoria(obtenerCategoria(producto.getCategoria()));
        return productoRepository.save(producto);
    }

    public Producto actualizar(String id, Producto datosActualizados) {
        validarProducto(datosActualizados, false);

        Producto producto = obtenerPorId(id);
        producto.setNombre(datosActualizados.getNombre());
        producto.setDescripcion(datosActualizados.getDescripcion());
        producto.setPrecio(datosActualizados.getPrecio());
        producto.setStock(datosActualizados.getStock());
        producto.setOferta(datosActualizados.getOferta());
        producto.setPrecioOferta(datosActualizados.getPrecioOferta());
        producto.setImagen(datosActualizados.getImagen());
        producto.setCategoria(obtenerCategoria(datosActualizados.getCategoria()));
        return productoRepository.save(producto);
    }

    public void eliminar(String id) {
        Producto producto = obtenerPorId(id);
        productoRepository.delete(producto);
    }

    public BigDecimal obtenerPrecioFinal(Producto producto) {
        if (Boolean.TRUE.equals(producto.getOferta())
                && producto.getPrecioOferta() != null) {
            return producto.getPrecioOferta();
        }
        return producto.getPrecio();
    }

    private Categoria obtenerCategoria(Categoria categoria) {
        if (categoria == null || categoria.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La categoría es obligatoria");
        }
        return categoriaRepository.findById(categoria.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "La categoría no existe"));
    }

    private void verificarCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El ID de la categoría es obligatorio");
        }
        if (!categoriaRepository.existsById(categoriaId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Categoría no encontrada");
        }
    }

    private void validarProducto(Producto producto, boolean requiereId) {
        if (producto == null
                || producto.getNombre() == null || producto.getNombre().isBlank()
                || producto.getPrecio() == null || producto.getStock() == null
                || producto.getStock() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre, precio y stock válido son obligatorios");
        }
        if (requiereId && (producto.getId() == null || producto.getId().isBlank())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El ID del producto es obligatorio");
        }
        if (producto.getPrecio().signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El precio no puede ser negativo");
        }
        if (Boolean.TRUE.equals(producto.getOferta())
                && (producto.getPrecioOferta() == null
                || producto.getPrecioOferta().signum() < 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Una oferta debe tener un precio válido");
        }
    }
}