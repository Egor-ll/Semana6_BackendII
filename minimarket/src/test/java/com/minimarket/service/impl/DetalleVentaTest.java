package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.repository.DetalleVentaRepository;
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
class DetalleVentaServiceImplTest {

    @Mock
    private DetalleVentaRepository detalleVentaRepository;

    @InjectMocks
    private DetalleVentaServiceImpl detalleVentaService;

    @Test
    void testFindAll() {
        DetalleVenta detalle1 = new DetalleVenta();
        DetalleVenta detalle2 = new DetalleVenta();

        when(detalleVentaRepository.findAll())
                .thenReturn(Arrays.asList(detalle1, detalle2));

        List<DetalleVenta> resultado = detalleVentaService.findAll();

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(detalleVentaRepository).findAll();
    }

    @Test
    void testFindById() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(1L);

        when(detalleVentaRepository.findById(1L))
                .thenReturn(Optional.of(detalle));

        DetalleVenta resultado = detalleVentaService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(detalleVentaRepository).findById(1L);
    }

    @Test
    void testSave() {
        DetalleVenta detalle = new DetalleVenta();

        when(detalleVentaRepository.save(detalle))
                .thenReturn(detalle);

        DetalleVenta resultado = detalleVentaService.save(detalle);

        assertNotNull(resultado);
        verify(detalleVentaRepository).save(detalle);
    }

    @Test
    void testDeleteById() {
        doNothing().when(detalleVentaRepository).deleteById(1L);

        detalleVentaService.deleteById(1L);

        verify(detalleVentaRepository).deleteById(1L);
    }
}