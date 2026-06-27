package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    private static final String R_ADMIN = "ADMINISTRADOR";
    private static final String R_VENDEDOR = "VENDEDOR";
    private static final String R_CLIENTE = "CLIENTE";

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Helper flexible para crear Rol, funcionará si Rol tiene:
    // - constructor(String), o
    // - setter "setNombre"/"setName", o
    // - campo "nombre"
    private Rol rol(String nombre) {
        try {
            Constructor<Rol> c = Rol.class.getConstructor(String.class);
            return c.newInstance(nombre);
        } catch (Exception ignored) {
        }
        try {
            Rol r = Rol.class.getDeclaredConstructor().newInstance();
            try {
                Method m = Rol.class.getMethod("setNombre", String.class);
                m.invoke(r, nombre);
                return r;
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Method m2 = Rol.class.getMethod("setName", String.class);
                m2.invoke(r, nombre);
                return r;
            } catch (NoSuchMethodException ignored) {
            }
            try {
                Field f = Rol.class.getDeclaredField("nombre");
                f.setAccessible(true);
                f.set(r, nombre);
                return r;
            } catch (NoSuchFieldException ignored) {
            }
            // si ninguno existe, devolver la instancia vacía (posible fallo más adelante)
            return r;
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear Rol por reflexión", ex);
        }
    }

    @Test
    void validarCamposObligatorios_usuarioCompleto_retornaTrue() {
        Usuario u = new Usuario();
        u.setUsername("juan");
        u.setPassword("password123");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol(R_VENDEDOR));
        u.setRoles(roles);

        assertTrue(usuarioService.validarCamposObligatorios(u));
    }

    @Test
    void validarCamposObligatorios_faltanCampos_retornaFalse() {
        Usuario u1 = new Usuario(); // todo nulo
        assertFalse(usuarioService.validarCamposObligatorios(u1));

        Usuario u2 = new Usuario();
        u2.setUsername("ana");
        u2.setPassword("pwd");
        u2.setRoles(new HashSet<>()); // roles vacío
        assertFalse(usuarioService.validarCamposObligatorios(u2));

        Usuario u3 = new Usuario();
        u3.setUsername(null);
        u3.setPassword("pwd");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol(R_CLIENTE));
        u3.setRoles(roles);
        assertFalse(usuarioService.validarCamposObligatorios(u3));
    }

    @Test
    void tienePermisoParaRegistrarVentas_adminOVendedor_retornaTrue() {
        Usuario admin = new Usuario();
        Set<Rol> r1 = new HashSet<>();
        r1.add(rol(R_ADMIN));
        admin.setRoles(r1);
        assertTrue(usuarioService.tienePermisoParaRegistrarVentas(admin));

        Usuario vendedor = new Usuario();
        Set<Rol> r2 = new HashSet<>();
        r2.add(rol(R_VENDEDOR));
        vendedor.setRoles(r2);
        assertTrue(usuarioService.tienePermisoParaRegistrarVentas(vendedor));
    }

    @Test
    void tienePermisoParaRegistrarVentas_clienteOSinRoles_retornaFalse() {
        Usuario cliente = new Usuario();
        Set<Rol> rc = new HashSet<>();
        rc.add(rol(R_CLIENTE));
        cliente.setRoles(rc);
        assertFalse(usuarioService.tienePermisoParaRegistrarVentas(cliente));

        Usuario sinRoles = new Usuario();
        sinRoles.setRoles(new HashSet<>());
        assertFalse(usuarioService.tienePermisoParaRegistrarVentas(sinRoles));

        assertFalse(usuarioService.tienePermisoParaRegistrarVentas(null));
    }

    @Test
    void save_encodesWhenRawPasswordAndTrimsUsername_andDelegatesToRepository() {
        Usuario u = new Usuario();
        u.setUsername("  pedro  ");
        u.setPassword("rawPassword");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol(R_VENDEDOR));
        u.setRoles(roles);

        when(passwordEncoder.encode("rawPassword")).thenReturn("ENCODED");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario arg = inv.getArgument(0);
            arg.setId(42L);
            return arg;
        });

        Usuario saved = usuarioService.save(u);

        assertNotNull(saved);
        assertEquals(42L, saved.getId());
        assertEquals("ENCODED", saved.getPassword());
        // username trimmed before save
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("pedro", captor.getValue().getUsername());

        verify(passwordEncoder).encode("rawPassword");
    }

    @Test
    void save_doesNotReencodeIfAlreadyEncodedPassword() {
        Usuario u = new Usuario();
        u.setUsername("marta");
        // Simula password con prefijo bcrypt ($2a$ / $2b$ / $2y$)
        u.setPassword("$2a$abcdefg");
        Set<Rol> roles = new HashSet<>();
        roles.add(rol(R_VENDEDOR));
        u.setRoles(roles);

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario arg = inv.getArgument(0);
            arg.setId(7L);
            return arg;
        });

        Usuario saved = usuarioService.save(u);

        assertNotNull(saved);
        assertEquals(7L, saved.getId());
        // no se llamó a passwordEncoder.encode
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void save_nullUsuario_lanzaNPE() {
        assertThrows(NullPointerException.class, () -> usuarioService.save(null));
    }

    @Test
    void findByUsername_trimsAndDelegates() {
        Usuario u = new Usuario();
        u.setId(5L);
        u.setUsername("maria");

        when(usuarioRepository.findByUsername("maria")).thenReturn(Optional.of(u));

        Optional<Usuario> result = usuarioService.findByUsername("  maria  ");
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getId());

        // verifica que el repo fue llamado con el username limpio (trim)
        verify(usuarioRepository).findByUsername("maria");
    }

    @Test
    void findByUsername_null_lanzaNPE() {
        assertThrows(NullPointerException.class, () -> usuarioService.findByUsername(null));
    }

    @Test
    void findAll_delegates() {
        Usuario a = new Usuario(); a.setId(1L);
        Usuario b = new Usuario(); b.setId(2L);
        when(usuarioRepository.findAll()).thenReturn(List.of(a, b));

        List<Usuario> lista = usuarioService.findAll();
        assertEquals(2, lista.size());
        verify(usuarioRepository).findAll();
    }

    @Test
    void findById_delegates() {
        Usuario u = new Usuario(); u.setId(9L);
        when(usuarioRepository.findById(9L)).thenReturn(Optional.of(u));

        Optional<Usuario> res = usuarioService.findById(9L);
        assertTrue(res.isPresent());
        assertEquals(9L, res.get().getId());
        verify(usuarioRepository).findById(9L);
    }

    @Test
    void findById_null_lanzaNPE() {
        assertThrows(NullPointerException.class, () -> usuarioService.findById(null));
    }

    @Test
    void deleteById_delegates() {
        doNothing().when(usuarioRepository).deleteById(3L);
        usuarioService.deleteById(3L);
        verify(usuarioRepository).deleteById(3L);
    }

    @Test
    void deleteById_null_lanzaNPE() {
        assertThrows(NullPointerException.class, () -> usuarioService.deleteById(null));
    }
}