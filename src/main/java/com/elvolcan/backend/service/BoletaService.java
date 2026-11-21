package com.elvolcan.backend.service;

import com.elvolcan.backend.dto.CrearBoletaRequest;
import com.elvolcan.backend.dto.ItemCarritoRequest;
import com.elvolcan.backend.model.Boleta;
import com.elvolcan.backend.model.DetalleBoleta;
import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.model.Usuario;
import com.elvolcan.backend.repository.BoletaRepository;
import com.elvolcan.backend.repository.ProductoRepository;
import com.elvolcan.backend.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoService productoService;

    public BoletaService(BoletaRepository boletaRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository,
                         ProductoService productoService) {
        this.boletaRepository = boletaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.productoService = productoService;
    }

    @Transactional
    public Boleta comprar(CrearBoletaRequest request, String email) {
        if (request == null || request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El carrito no puede estar vacío");
        }

        List<ItemCarritoRequest> items = new ArrayList<>(request.items());
        for (ItemCarritoRequest item : items) {
            if (item == null || item.productoId() == null || item.productoId().isBlank()
                    || item.cantidad() == null || item.cantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada producto debe tener un ID y una cantidad válida");
            }
        }

        Set<String> productosIncluidos = new HashSet<>();
        for (ItemCarritoRequest item : items) {
            if (!productosIncluidos.add(item.productoId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No repitas un producto en el carrito");
            }
        }

        items.sort((a, b) -> a.productoId().compareTo(b.productoId()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Boleta boleta = new Boleta();
        boleta.setUsuario(usuario);
        boleta.setFecha(LocalDateTime.now());
        boleta.setTotal(BigDecimal.ZERO);

        for (ItemCarritoRequest item : items) {
            Producto producto = productoRepository.buscarPorIdConBloqueo(item.productoId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Producto no encontrado: " + item.productoId()));

            if (producto.getStock() < item.cantidad()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No hay stock suficiente para " + producto.getNombre());
            }

            BigDecimal precio = productoService.obtenerPrecioFinal(producto);
            BigDecimal subtotal = precio.multiply(BigDecimal.valueOf(item.cantidad()));

            producto.setStock(producto.getStock() - item.cantidad());

            DetalleBoleta detalle = new DetalleBoleta();
            detalle.setBoleta(boleta);
            detalle.setProducto(producto);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(subtotal);
            boleta.getDetalles().add(detalle);

            boleta.setTotal(boleta.getTotal().add(subtotal));
        }

        return boletaRepository.save(boleta);
    }

    @Transactional(readOnly = true)
    public List<Boleta> obtenerHistorial() {
        return boletaRepository.findAllByOrderByFechaDesc();
    }

    @Transactional(readOnly = true)
    public List<Boleta> obtenerMisCompras(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión");
        }
        return boletaRepository.findAllByUsuarioEmailOrderByFechaDesc(email);
    }

    @Transactional(readOnly = true)
    public Boleta obtenerPorId(Long id, String email, boolean esStaff) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Boleta no encontrada"));

        if (!esStaff && !boleta.getUsuario().getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes consultar boletas de otros usuarios");
        }
        return boleta;
    }

    @Transactional
    public Boleta anular(Long id) {
        Boleta boleta = boletaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Boleta no encontrada"));

        if ("ANULADA".equals(boleta.getEstado())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La boleta ya está anulada");
        }

        for (DetalleBoleta detalle : boleta.getDetalles()) {
            String productoId = detalle.getProducto().getId();
            Producto producto = productoRepository.buscarPorIdConBloqueo(productoId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Producto no encontrado: " + productoId));
            producto.setStock(producto.getStock() + detalle.getCantidad());
        }

        boleta.setEstado("ANULADA");
        return boletaRepository.save(boleta);
    }
}
