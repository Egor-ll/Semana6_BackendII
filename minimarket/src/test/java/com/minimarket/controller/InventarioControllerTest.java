package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Inventario;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.ActividadSeguridadService;
import com.minimarket.service.InventarioService;
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

@WebMvcTest(controllers = InventarioController.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InventarioService inventarioService;

    // --- MOCKS DE SEGURIDAD PARA QUE EL CONTEXTO CARGUE ---
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private ActividadSeguridadService actividadSeguridadService;
   

    private Inventario createMockInventario(Long id) {
        Inventario inv = new Inventario();
        inv.setId(id);
        inv.setTipoMovimiento("ENTRADA");
        inv.setCantidad(10);
        return inv;
    }

    @Test
    @DisplayName("GET /api/inventario - listar movimientos (EMPLEADO)")
    @WithMockUser(roles = "EMPLEADO")
    void listarMovimientos_deInventario_returnsOk() throws Exception {
        when(inventarioService.findAll()).thenReturn(List.of(createMockInventario(1L)));

        mockMvc.perform(get("/api/inventario")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/inventario/{id} - id inválido -> 400")
    @WithMockUser(roles = "EMPLEADO")
    void obtenerMovimientoPorId_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/inventario/0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("debe ser válido")));
    }

    @Test
    @DisplayName("POST /api/inventario - registrar -> 201 + CSRF")
    @WithMockUser(roles = "EMPLEADO")
    void registrarMovimiento_success_returnsCreated() throws Exception {
        Inventario input = createMockInventario(null);
        
        when(inventarioService.save(org.mockito.ArgumentMatchers.any(Inventario.class)))
                .thenAnswer(invocation -> {
                    Inventario i = invocation.getArgument(0);
                    i.setId(50L);
                    return i;
                });

        mockMvc.perform(post("/api/inventario")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(50)));
    }

    @Test
    @DisplayName("DELETE /api/inventario/{id} - con ADMIN -> 204")
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminarMovimiento_withAdmin_returnsNoContent() throws Exception {
        Inventario inv = createMockInventario(9L);
        when(inventarioService.findById(9L)).thenReturn(inv);
        doNothing().when(inventarioService).deleteById(9L);

        mockMvc.perform(delete("/api/inventario/9")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(inventarioService).deleteById(9L);
    }

    @Test
    @DisplayName("DELETE /api/inventario/{id} - 404 si no existe")
    @WithMockUser(roles = "ADMINISTRADOR")
    void eliminarMovimiento_notFound_returns404() throws Exception {
        when(inventarioService.findById(99L)).thenReturn(null);

        mockMvc.perform(delete("/api/inventario/99")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}