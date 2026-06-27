package com.minimarket.service;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.service.impl.CategoriaServiceImpl;

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
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    @Test
    void testFindAll() {
        Categoria categoria1 = new Categoria();
        Categoria categoria2 = new Categoria();

        when(categoriaRepository.findAll()).thenReturn(Arrays.asList(categoria1, categoria2));

        List<Categoria> resultado = categoriaService.findAll();

        assertEquals(2, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(categoriaRepository).findById(1L);
    }

    @Test
    void testSave() {
        Categoria categoria = new Categoria();

        when(categoriaRepository.save(categoria)).thenReturn(categoria);

        Categoria resultado = categoriaService.save(categoria);

        assertNotNull(resultado);
        verify(categoriaRepository).save(categoria);
    }

    @Test
    void testDeleteById() {
        doNothing().when(categoriaRepository).deleteById(1L);

        categoriaService.deleteById(1L);

        verify(categoriaRepository).deleteById(1L);
    }
}