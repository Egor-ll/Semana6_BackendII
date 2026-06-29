package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.ActividadSeguridadService;
import com.minimarket.service.ProductoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductoController.class)
class ProductoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private ActividadSeguridadService actividadSeguridadService;

    private Categoria createMockCategoria() {
        Categoria cat = new Categoria();
        cat.setId(1L);
        cat.setNombre("Alimentos");
        return cat;
    }

    @Test
    @DisplayName("GET /api/productos - listar con rol CLIENTE")
    @WithMockUser(roles = "CLIENTE")
    void listarProductos_withClienteRole_returnsOk() throws Exception {
        Producto p1 = new Producto(); 
        p1.setId(1L); 
        p1.setNombre("Arroz");
        when(productoService.findAll()).thenReturn(List.of(p1));

        mockMvc.perform(get("/api/productos")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("Arroz")));
    }

    @Test
    @DisplayName("POST /api/productos - crear con rol EMPLEADO + CSRF")
    @WithMockUser(roles = "EMPLEADO")
    void guardarProducto_withEmpleado_sanitizesAndReturnsCreated() throws Exception {
        Producto input = new Producto();
        // Cambiado a un nombre sin etiquetas HTML para cumplir con la validación del controller
        input.setNombre("Arroz Blanco");
        input.setPrecio(100.0);
        input.setStock(10);
        input.setCategoria(createMockCategoria());

        when(productoService.save(org.mockito.ArgumentMatchers.any(Producto.class)))
                .thenAnswer(i -> {
                    Producto p = i.getArgument(0);
                    p.setId(99L);
                    return p;
                });

        mockMvc.perform(post("/api/productos")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.nombre", is("Arroz Blanco")));
    }

    @Test
    @DisplayName("PUT /api/productos/{id} - éxito con rol EMPLEADO")
    @WithMockUser(roles = "EMPLEADO")
    void actualizarProducto_success_returnsOk() throws Exception {
        Categoria cat = createMockCategoria();
        
        Producto existente = new Producto();
        existente.setId(7L);
        existente.setNombre("Original");
        existente.setPrecio(50.0);
        existente.setStock(5);
        existente.setCategoria(cat);

        when(productoService.findById(7L)).thenReturn(existente);
        when(productoService.save(org.mockito.ArgumentMatchers.any(Producto.class)))
                .thenAnswer(i -> i.getArgument(0));

        Producto rawInput = new Producto();
        rawInput.setNombre("Editado");
        rawInput.setPrecio(60.0);
        rawInput.setStock(7);
        rawInput.setCategoria(cat);

        mockMvc.perform(put("/api/productos/7")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rawInput))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Editado")));
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - requiere ADMINISTRADOR")
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminarProducto_withAdmin_returnsNoContent() throws Exception {
        Producto p = new Producto(); 
        p.setId(20L);
        when(productoService.findById(20L)).thenReturn(p);
        doNothing().when(productoService).deleteById(20L);

        mockMvc.perform(delete("/api/productos/20")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(productoService).deleteById(20L);
    }

    @Test
    @DisplayName("DELETE /api/productos/{id} - 404 si no existe (rol EMPLEADO)")
    @WithMockUser(roles = "EMPLEADO")
    void eliminarProducto_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/productos/30")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/productos/{id} - id inválido -> 400")
    @WithMockUser(roles = "CLIENTE")
    void obtenerProducto_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/productos/0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("debe ser válido")));
    }
}