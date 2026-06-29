package com.minimarket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.security.filter.JwtAuthenticationFilter;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.service.UsuarioService;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UsuarioController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                UserDetailsServiceAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void listarUsuarios_returnsList() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("admin");

        when(usuarioService.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].username", is("admin")));

        verify(usuarioService).findAll();
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void obtenerUsuarioPorId_found() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("juan");

        when(usuarioService.findById(1L)).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/usuarios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("juan")));

        verify(usuarioService).findById(1L);
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void obtenerUsuarioPorId_notFound() throws Exception {
        when(usuarioService.findById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/usuarios/{id}", 2L))
                .andExpect(status().isNotFound());

        verify(usuarioService).findById(2L);
    }

    @Test
    @WithMockUser(roles = {"EMPLEADO"})
    void obtenerUsuarioPorId_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/usuarios/{id}", 0L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El id del usuario debe ser válido")));

        verify(usuarioService, never()).findById(anyLong());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void guardarUsuario_sanitizesUsername_andReturnsCreated() throws Exception {
        // build JSON explicitly so password (WRITE_ONLY) is included in request body
        ObjectNode userJson = buildUsuarioJson("  juan  ", "12345678", "ADMINISTRADOR");

        when(usuarioService.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userJson)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("juan")));

        verify(usuarioService).save(any(Usuario.class));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void actualizarUsuario_found_updatesAndReturnsOk() throws Exception {
        ObjectNode userJson = buildUsuarioJson("  maria  ", "12345678", "ADMINISTRADOR");

        Usuario existing = new Usuario();
        existing.setId(5L);

        when(usuarioService.findById(5L)).thenReturn(Optional.of(existing));
        when(usuarioService.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(5L);
            return u;
        });

        mockMvc.perform(put("/api/usuarios/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userJson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.username", is("maria")));

        verify(usuarioService).findById(5L);
        verify(usuarioService).save(any(Usuario.class));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void actualizarUsuario_invalidId_returnsBadRequest() throws Exception {
        ObjectNode userJson = buildUsuarioJson("test", "12345678", "ADMINISTRADOR");

        mockMvc.perform(put("/api/usuarios/{id}", 0L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userJson)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El id del usuario debe ser válido")));

        verify(usuarioService, never()).findById(anyLong());
        verify(usuarioService, never()).save(any(Usuario.class));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void actualizarUsuario_notFound_returns404() throws Exception {
        ObjectNode userJson = buildUsuarioJson("test", "12345678", "ADMINISTRADOR");

        when(usuarioService.findById(10L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/usuarios/{id}", 10L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userJson)))
                .andExpect(status().isNotFound());

        verify(usuarioService).findById(10L);
        verify(usuarioService, never()).save(any(Usuario.class));
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void eliminarUsuario_found_returnsNoContent() throws Exception {
        Usuario u = new Usuario();
        u.setId(1L);

        when(usuarioService.findById(1L)).thenReturn(Optional.of(u));
        doNothing().when(usuarioService).deleteById(1L);

        mockMvc.perform(delete("/api/usuarios/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(usuarioService).findById(1L);
        verify(usuarioService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void eliminarUsuario_notFound_returns404() throws Exception {
        when(usuarioService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/usuarios/{id}", 99L))
                .andExpect(status().isNotFound());

        verify(usuarioService).findById(99L);
        verify(usuarioService, never()).deleteById(anyLong());
    }

    @Test
    @WithMockUser(roles = {"ADMINISTRADOR"})
    void eliminarUsuario_invalidId_returnsBadRequest() throws Exception {
        mockMvc.perform(delete("/api/usuarios/{id}", -1L))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("El id del usuario debe ser válido")));

        verify(usuarioService, never()).findById(anyLong());
        verify(usuarioService, never()).deleteById(anyLong());
    }

    
    private ObjectNode buildUsuarioJson(String username, String password, String rolNombre) {
        ObjectNode userJson = objectMapper.createObjectNode();
        userJson.put("username", username == null ? null : username.trim());
        userJson.put("password", password);
        ArrayNode rolesArray = userJson.putArray("roles");
        ObjectNode roleNode = objectMapper.createObjectNode();
        roleNode.put("nombre", rolNombre);
        rolesArray.add(roleNode);
        return userJson;
    }
}