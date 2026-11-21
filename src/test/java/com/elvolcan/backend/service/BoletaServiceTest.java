package com.elvolcan.backend.service;

import com.elvolcan.backend.dto.CrearBoletaRequest;
import com.elvolcan.backend.dto.ItemCarritoRequest;
import com.elvolcan.backend.model.Boleta;
import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.model.Usuario;
import com.elvolcan.backend.repository.BoletaRepository;
import com.elvolcan.backend.repository.ProductoRepository;
import com.elvolcan.backend.repository.UsuarioRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoletaServiceTest {

    @Mock private BoletaRepository boletaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProductoService productoService;

    private BoletaService boletaService;

    @BeforeEach
    void setUp() {
        boletaService = new BoletaService(
                boletaRepository,
                productoRepository,
                usuarioRepository,
                productoService);
    }

    @Test
    void deberiaCrearBoletaYDescontarStock() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@elvolcan.cl");
        Producto producto = new Producto();
        producto.setId("CL001");
        producto.setNombre("Cilindro 5 kg");
        producto.setStock(5);
        CrearBoletaRequest request = new CrearBoletaRequest(
                List.of(new ItemCarritoRequest("CL001", 2)));

        when(usuarioRepository.findByEmail("cliente@elvolcan.cl")).thenReturn(Optional.of(usuario));
        when(productoRepository.buscarPorIdConBloqueo("CL001")).thenReturn(Optional.of(producto));
        when(productoService.obtenerPrecioFinal(producto)).thenReturn(new BigDecimal("6500"));
        when(boletaRepository.save(any(Boleta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Boleta resultado = boletaService.comprar(request, "cliente@elvolcan.cl");

        assertEquals(3, producto.getStock());
        assertEquals(new BigDecimal("13000"), resultado.getTotal());
        assertEquals(1, resultado.getDetalles().size());
        verify(boletaRepository).save(resultado);
    }

    @Test
    void deberiaRechazarCompraSinStockSuficiente() {
        Usuario usuario = new Usuario();
        usuario.setEmail("cliente@elvolcan.cl");
        Producto producto = new Producto();
        producto.setId("CL001");
        producto.setNombre("Cilindro 5 kg");
        producto.setStock(1);
        CrearBoletaRequest request = new CrearBoletaRequest(
                List.of(new ItemCarritoRequest("CL001", 2)));

        when(usuarioRepository.findByEmail("cliente@elvolcan.cl")).thenReturn(Optional.of(usuario));
        when(productoRepository.buscarPorIdConBloqueo("CL001")).thenReturn(Optional.of(producto));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> boletaService.comprar(request, "cliente@elvolcan.cl"));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(1, producto.getStock());
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarProductosRepetidosEnElCarrito() {
        CrearBoletaRequest request = new CrearBoletaRequest(List.of(
                new ItemCarritoRequest("CL001", 1),
                new ItemCarritoRequest("CL001", 2)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> boletaService.comprar(request, "cliente@elvolcan.cl"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository, never()).findByEmail(any());
        verify(productoRepository, never()).buscarPorIdConBloqueo(any());
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void deberiaRechazarCarritoVacio() {
        CrearBoletaRequest request = new CrearBoletaRequest(List.of());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> boletaService.comprar(request, "cliente@elvolcan.cl"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(usuarioRepository, never()).findByEmail(any());
        verify(boletaRepository, never()).save(any());
    }

    @Test
    void deberiaConsultarSoloLasComprasDelUsuario() {
        Boleta boleta = new Boleta();
        boleta.setId(10L);
        when(boletaRepository.findAllByUsuarioEmailOrderByFechaDesc("cliente@elvolcan.cl"))
                .thenReturn(List.of(boleta));

        List<Boleta> resultado = boletaService.obtenerMisCompras("cliente@elvolcan.cl");

        assertEquals(List.of(boleta), resultado);
        verify(boletaRepository)
                .findAllByUsuarioEmailOrderByFechaDesc("cliente@elvolcan.cl");
    }
}
