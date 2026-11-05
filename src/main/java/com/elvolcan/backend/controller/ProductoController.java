package com.elvolcan.backend.controller;

import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Gestion de cilindros y catalogo de El Volcan")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Trae todo el catalogo disponible")
    public List<Producto> listarTodos(@RequestParam(required = false) Long categoriaId) {
        if (categoriaId != null) {
            return productoService.obtenerPorCategoria(categoriaId);
        }
        return productoService.obtenerTodos();
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Buscar productos por categoria",
            description = "Devuelve los productos asociados a una categoría")
    public List<Producto> listarPorCategoria(@PathVariable Long categoriaId) {
        return productoService.obtenerPorCategoria(categoriaId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID")
    public Producto obtenerPorId(@PathVariable String id) {
        return productoService.obtenerPorId(id);
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Obtener ofertas", description = "Filtra y devuelve solo los productos que estan actualmente en oferta")
    public List<Producto> listarOfertas() {
        return productoService.obtenerOfertas();
    }

    @PostMapping
    @Operation(summary = "Crear un producto")
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoService.crear(producto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un producto")
    public Producto actualizar(@PathVariable String id,
                               @RequestBody Producto producto) {
        return productoService.actualizar(id, producto);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un producto")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}