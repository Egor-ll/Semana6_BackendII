package com.minimarket.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.CarritoServiceImpl;

@ExtendWith(MockitoExtension.class)
class CarritoServicelmplTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    @Test
    void testFindAll() {
        Carrito carrito = new Carrito();
        when(carritoRepository.findAll()).thenReturn(Arrays.asList(carrito));

        List<Carrito> resultado = carritoService.findAll();

        assertEquals(1, resultado.size());
        verify(carritoRepository).findAll();
    }

    @Test
    void testFindById() {
        Carrito carrito = new Carrito();
        carrito.setId(1L);

        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carrito));

        Carrito resultado = carritoService.findById(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(carritoRepository).findById(1L);
    }

    @Test
    void testSave() {
        Carrito carrito = new Carrito();

        when(carritoRepository.save(carrito)).thenReturn(carrito);

        Carrito resultado = carritoService.save(carrito);

        assertNotNull(resultado);
        verify(carritoRepository).save(carrito);
    }

    @Test
    void testDeleteById() {
        carritoService.deleteById(1L);

        verify(carritoRepository).deleteById(1L);
    }

    @Test
    void testFindByUsuarioId() {
        Carrito carrito = new Carrito();

        when(carritoRepository.findByUsuarioId(1L))
                .thenReturn(Arrays.asList(carrito));

        List<Carrito> resultado = carritoService.findByUsuarioId(1L);

        assertEquals(1, resultado.size());
        verify(carritoRepository).findByUsuarioId(1L);
    }

    //  Tests de validación de parámetros 

    @Test
    void testFindByIdNull() {
        assertThrows(NullPointerException.class, () -> carritoService.findById(null));
    }

    @Test
    void testSaveNull() {
        assertThrows(NullPointerException.class, () -> carritoService.save(null));
    }

    @Test
    void testDeleteByIdNull() {
        assertThrows(NullPointerException.class, () -> carritoService.deleteById(null));
    }

    @Test
    void testFindByUsuarioIdNull() {
        assertThrows(NullPointerException.class,
                () -> carritoService.findByUsuarioId(null));
    }

    //  Tests de comportamiento: agregarProducto 
    // Requieren que CarritoServiceImpl tenga:
    // public Carrito agregarProducto(Long productoId, Long usuarioId, Integer cantidad)
    @Test
    void agregarProducto_guardaCarritoCuandoHayStockSuficiente() {
        Producto p = new Producto();
        p.setId(1L);
        p.setStock(10);

        Usuario u = new Usuario();
        u.setId(2L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Carrito savedResult = carritoService.agregarProducto(1L, 2L, 3);

        // Verificaciones sobre el Carrito retornado
        assertNotNull(savedResult);
        assertSame(p, savedResult.getProducto());
        assertSame(u, savedResult.getUsuario());
        assertEquals(3, savedResult.getCantidad());

        // Verificar que se guardó el carrito
        ArgumentCaptor<Carrito> captor = ArgumentCaptor.forClass(Carrito.class);
        verify(carritoRepository, times(1)).save(captor.capture());
        Carrito saved = captor.getValue();
        assertSame(p, saved.getProducto());
        assertSame(u, saved.getUsuario());
        assertEquals(3, saved.getCantidad());

        // Verificar reducción y guardado del producto
        ArgumentCaptor<Producto> productoCaptor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository, times(1)).save(productoCaptor.capture());
        Producto savedProducto = productoCaptor.getValue();
        assertEquals(7, savedProducto.getStock()); // 10 - 3 = 7
    }

    @Test
    void agregarProducto_lanzaCuandoStockInsuficienteYNoGuarda() {
        Producto p = new Producto();
        p.setId(1L);
        p.setStock(2);

        Usuario u = new Usuario();
        u.setId(2L);

        when(productoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(u));

        assertThrows(IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 2L, 5));

        verify(carritoRepository, never()).save(any());
        verify(productoRepository, never()).save(any());
    }
}