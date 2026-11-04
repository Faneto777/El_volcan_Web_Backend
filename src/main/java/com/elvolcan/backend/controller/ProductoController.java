package com.elvolcan.backend.controller;

import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Gestion de cilindros y catalogo de El Volcan")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(summary = "Listar todos los productos", description = "Trae todo el catalogo disponible")
    public List<Producto> listarTodos() {
        return productoService.obtenerTodos();
    }

    @GetMapping("/ofertas")
    @Operation(summary = "Obtener ofertas", description = "Filtra y devuelve solo los productos que estan actualmente en oferta")
    public List<Producto> listarOfertas() {
        return productoService.obtenerOfertas();
    }
}