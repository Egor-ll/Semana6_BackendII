package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @Test
    void testFindAll() {
        Inventario inventario1 = new Inventario();
        Inventario inventario2 = new Inventario();

        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventario1, inventario2));

        List<Inventario> resultado = inventarioService.findAll();

        assertEquals(2, resultado.size());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        Inventario inventario = new Inventario();
        inventario.setId(1L);

        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventario));

        Inventario resultado = inventarioService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(inventarioRepository).findById(1L);
    }

    @Test
    void testSave() {
        Inventario inventario = new Inventario();

        when(inventarioRepository.save(inventario)).thenReturn(inventario);

        Inventario resultado = inventarioService.save(inventario);

        assertNotNull(resultado);
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void testDeleteById() {
        doNothing().when(inventarioRepository).deleteById(1L);

        inventarioService.deleteById(1L);

        verify(inventarioRepository).deleteById(1L);
    }
}