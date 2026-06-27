package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.repository.RolRepository;
import com.minimarket.service.impl.RolServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolServiceTest {

    @Mock
    private RolRepository rolRepository;

    @InjectMocks
    private RolServiceImpl rolService;

    @Test
    void testFindByNombreExistente() {
        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        when(rolRepository.findByNombre("ADMIN"))
                .thenReturn(Optional.of(rol));

        Optional<Rol> resultado = rolService.findByNombre("ADMIN");

        assertTrue(resultado.isPresent());
        assertEquals("ADMIN", resultado.get().getNombre());

        verify(rolRepository).findByNombre("ADMIN");
    }

    @Test
    void testFindByNombreNull() {
        Optional<Rol> resultado = rolService.findByNombre(null);

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(rolRepository);
    }

    @Test
    void testFindByNombreBlank() {
        Optional<Rol> resultado = rolService.findByNombre("   ");

        assertTrue(resultado.isEmpty());

        verifyNoInteractions(rolRepository);
    }

    @Test
    void testFindByNombreConEspacios() {
        Rol rol = new Rol();
        rol.setNombre("ADMIN");

        when(rolRepository.findByNombre("ADMIN"))
                .thenReturn(Optional.of(rol));

        Optional<Rol> resultado = rolService.findByNombre("  ADMIN  ");

        assertTrue(resultado.isPresent());
        assertEquals("ADMIN", resultado.get().getNombre());

        verify(rolRepository).findByNombre("ADMIN");
    }
}