package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = VentaController.class,
    excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class
    }
)
@AutoConfigureMockMvc(addFilters = false)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VentaService ventaService;

    // Estos dos MockBeans son necesarios para que el contexto de Spring cargue correctamente
    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void listarVentas_returnsList() throws Exception {
        Venta v = new Venta();
        v.setId(1L);

        when(ventaService.findAll()).thenReturn(List.of(v));

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(ventaService).findAll();
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void obtenerVentaPorId_found() throws Exception {
        Venta v = new Venta();
        v.setId(10L);

        when(ventaService.findById(10L)).thenReturn(v);

        mockMvc.perform(get("/api/ventas/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)));

        verify(ventaService).findById(10L);
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void crearVenta_valid_returnsCreated() throws Exception {
        ObjectNode ventaJson = objectMapper.createObjectNode();
        ObjectNode usuarioNode = objectMapper.createObjectNode();
        usuarioNode.put("id", 2L);
        ventaJson.set("usuario", usuarioNode);
        ventaJson.put("fecha", System.currentTimeMillis());

        ArrayNode detallesArr = ventaJson.putArray("detalles");
        ObjectNode det1 = objectMapper.createObjectNode();
        ObjectNode prod1 = objectMapper.createObjectNode();
        prod1.put("id", 3L);
        det1.set("producto", prod1);
        det1.put("cantidad", 2);
        det1.put("precio", 5.5);
        detallesArr.add(det1);

        Venta returned = new Venta();
        returned.setId(99L);
        Usuario u = new Usuario();
        u.setId(2L);
        returned.setUsuario(u);
        returned.setFecha(new Date());
        
        List<DetalleVenta> detalles = new ArrayList<>();
        DetalleVenta d1 = new DetalleVenta();
        Producto p1 = new Producto();
        p1.setId(3L);
        p1.setPrecio(5.5);
        d1.setProducto(p1);
        d1.setCantidad(2);
        d1.setPrecio(5.5);
        detalles.add(d1);
        returned.setDetalles(detalles);

        when(ventaService.save(any(Venta.class))).thenReturn(returned);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(99)))
                .andExpect(jsonPath("$.usuario.id", is(2)));

        verify(ventaService).save(any(Venta.class));
    }
}