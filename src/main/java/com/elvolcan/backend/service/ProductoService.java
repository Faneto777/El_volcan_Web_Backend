package com.elvolcan.backend.service;

import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerOfertas() {
        return productoRepository.findByOfertaTrue();
    }

    public Producto aplicarDescuentoSiCorresponde(Producto producto) {
        if (producto.getOferta() != null && producto.getOferta()) {
            producto.setPrecio(producto.getPrecioOferta());
        }
        return producto;
    }
}