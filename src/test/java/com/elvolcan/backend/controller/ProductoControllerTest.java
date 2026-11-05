package com.elvolcan.backend.controller;

import com.elvolcan.backend.model.Categoria;
import com.elvolcan.backend.model.Producto;
import com.elvolcan.backend.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new ProductoController(productoService)).build();
    }

    @Test
    void deberiaListarProductos() throws Exception {
        when(productoService.obtenerTodos()).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/v1/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("GLP-5"))
                .andExpect(jsonPath("$[0].nombre").value("Gas 5 kg"));
    }

    @Test
    void deberiaBuscarProductosPorCategoria() throws Exception {
        when(productoService.obtenerPorCategoria(1L)).thenReturn(List.of(producto()));

        mockMvc.perform(get("/api/v1/productos/categoria/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("GLP-5"));

        verify(productoService).obtenerPorCategoria(1L);
    }

    @Test
    void deberiaCrearProductoYResponder201() throws Exception {
        when(productoService.crear(any(Producto.class))).thenReturn(producto());

        mockMvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "id": "GLP-5",
                                  "nombre": "Gas 5 kg",
                                  "precio": 10000,
                                  "stock": 5,
                                  "categoria": {"id": 1}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("GLP-5"));
    }

    @Test
    void deberiaActualizarProducto() throws Exception {
        when(productoService.actualizar(eq("GLP-5"), any(Producto.class)))
                .thenReturn(producto());

        mockMvc.perform(put("/api/v1/productos/GLP-5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Gas 5 kg actualizado",
                                  "precio": 10500,
                                  "stock": 8,
                                  "categoria": {"id": 1}
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("GLP-5"));

        verify(productoService).actualizar(eq("GLP-5"), any(Producto.class));
    }

    @Test
    void deberiaEliminarProductoYResponder204() throws Exception {
        mockMvc.perform(delete("/api/v1/productos/GLP-5"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(productoService).eliminar("GLP-5");
    }

    private Producto producto() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Cilindros");

        Producto producto = new Producto();
        producto.setId("GLP-5");
        producto.setNombre("Gas 5 kg");
        producto.setPrecio(new BigDecimal("10000"));
        producto.setStock(5);
        producto.setCategoria(categoria);
        return producto;
    }
}
