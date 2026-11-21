package com.elvolcan.backend.controller;

import com.elvolcan.backend.dto.CrearBoletaRequest;
import com.elvolcan.backend.model.Boleta;
import com.elvolcan.backend.service.BoletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/boletas")
@Tag(name = "Boletas", description = "Compras e historial de ventas")
public class BoletaController {

    private final BoletaService boletaService;

    public BoletaController(BoletaService boletaService) {
        this.boletaService = boletaService;
    }

    @PostMapping
    @Operation(summary = "Finalizar compra", description = "Procesa los productos del carrito y crea la boleta")
    public ResponseEntity<Boleta> comprar(@Valid @RequestBody CrearBoletaRequest request,
                                          Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(boletaService.comprar(request, principal.getName()));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Consultar historial de ventas", description = "Lista las boletas ordenadas desde la más reciente")
    public List<Boleta> obtenerHistorial() {
        return boletaService.obtenerHistorial();
    }

    @GetMapping("/mis-compras")
    @Operation(summary = "Consultar mis compras", description = "Lista las boletas del usuario autenticado")
    public List<Boleta> obtenerMisCompras(Principal principal) {
        return boletaService.obtenerMisCompras(principal.getName());
    }
}
