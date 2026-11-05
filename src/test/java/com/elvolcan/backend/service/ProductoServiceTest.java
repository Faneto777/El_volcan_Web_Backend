package com.elvolcan.backend.service;

import com.elvolcan.backend.model.Categoria;
import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.repository.CategoriaRepository;
import com.elvolcan.backend.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private ProductoService productoService;

    @BeforeEach
    void setUp() {
        productoService = new ProductoService(productoRepository, categoriaRepository);
    }

    @Test
    void deberiaCrearProductoConCategoriaExistente() {
        Categoria categoria = categoria(1L);
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria);

        when(productoRepository.existsById("GLP-5")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(producto)).thenReturn(producto);

        Producto resultado = productoService.crear(producto);

        assertSame(producto, resultado);
        verify(productoRepository).save(producto);
    }

    @Test
    void deberiaRechazarIdDuplicado() {
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria(1L));
        when(productoRepository.existsById("GLP-5")).thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(producto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarStockNegativo() {
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, -1, categoria(1L));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(producto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarCategoriaInexistente() {
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria(99L));
        when(productoRepository.existsById("GLP-5")).thenReturn(false);
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(producto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarPrecioDeOfertaMayorOIgualAlNormal() {
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria(1L));
        producto.setOferta(true);
        producto.setPrecioOferta(new BigDecimal("10000"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(producto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void deberiaBuscarProductosPorCategoria() {
        Categoria categoria = categoria(1L);
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria);
        when(categoriaRepository.existsById(1L)).thenReturn(true);
        when(productoRepository.findByCategoriaId(1L)).thenReturn(List.of(producto));

        List<Producto> resultado = productoService.obtenerPorCategoria(1L);

        assertEquals(List.of(producto), resultado);
        verify(productoRepository).findByCategoriaId(1L);
    }

    @Test
    void deberiaCalcularPrecioDeOfertaSinModificarPrecioBase() {
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria(1L));
        producto.setOferta(true);
        producto.setPrecioOferta(new BigDecimal("8500"));

        BigDecimal resultado = productoService.obtenerPrecioFinal(producto);

        assertEquals(new BigDecimal("8500"), resultado);
        assertEquals(new BigDecimal("10000"), producto.getPrecio());
    }

    @Test
    void deberiaUsarPrecioNormalCuandoNoHayOferta() {
        Producto producto = producto("GLP-5", "Gas 5 kg", 10000, 5, categoria(1L));

        BigDecimal resultado = productoService.obtenerPrecioFinal(producto);

        assertEquals(new BigDecimal("10000"), resultado);
    }

    private Producto producto(String id, String nombre, int precio, int stock,
                              Categoria categoria) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(BigDecimal.valueOf(precio));
        producto.setStock(stock);
        producto.setCategoria(categoria);
        return producto;
    }

    private Categoria categoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre("Cilindros");
        return categoria;
    }
}
