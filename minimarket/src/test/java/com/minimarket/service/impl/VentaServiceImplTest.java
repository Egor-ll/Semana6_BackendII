package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceImplTest {

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void save_success_assignsPrecio_reducesStock_andSaves() {
        // Preparar venta
        Venta v = new Venta();
        v.setFecha(null); // dejar nulo para que el servicio asigne la fecha
        Usuario uRef = new Usuario();
        uRef.setId(1L);
        v.setUsuario(uRef);

        DetalleVenta det = new DetalleVenta();
        Producto pRef = new Producto();
        pRef.setId(10L);
        det.setProducto(pRef);
        det.setCantidad(2);
        v.setDetalles(List.of(det));

        // Mock usuario existente
        Usuario usuarioEnDb = new Usuario();
        usuarioEnDb.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioEnDb));

        // Mock producto con precio y stock suficiente
        Producto productoEnDb = new Producto();
        productoEnDb.setId(10L);
        productoEnDb.setPrecio(5.5);
        productoEnDb.setStock(10);
        productoEnDb.setNombre("Producto X");
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoEnDb));

        // Simular save del repositorio (retornar la venta con id)
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta arg = inv.getArgument(0);
            arg.setId(99L);
            return arg;
        });

        Venta saved = ventaService.save(v);

        assertNotNull(saved);
        assertEquals(99L, saved.getId());
        assertNotNull(saved.getFecha()); // fecha asignada por el servicio
        assertEquals(1, saved.getDetalles().size());

        DetalleVenta savedDet = saved.getDetalles().get(0);
        assertEquals(5.5, savedDet.getPrecio()); // precio asignado desde el producto
        assertEquals(2, savedDet.getCantidad());
        // El producto en el detalle debe reflejar stock reducido
        assertEquals(8, savedDet.getProducto().getStock());

        // Verificaciones de llamadas
        verify(usuarioRepository).findById(1L);
        verify(productoRepository).findById(10L);
        verify(ventaRepository).save(any(Venta.class));
    }

    @Test
    void save_whenStockInsufficient_throwsIllegalArgumentException() {
        Venta v = new Venta();
        Usuario uRef = new Usuario();
        uRef.setId(2L);
        v.setUsuario(uRef);

        DetalleVenta det = new DetalleVenta();
        Producto pRef = new Producto();
        pRef.setId(20L);
        det.setProducto(pRef);
        det.setCantidad(5);
        v.setDetalles(List.of(det));

        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(new Usuario()));

        Producto productoEnDb = new Producto();
        productoEnDb.setId(20L);
        productoEnDb.setPrecio(2.0);
        productoEnDb.setStock(2); // stock insuficiente
        productoEnDb.setNombre("Producto Y");
        when(productoRepository.findById(20L)).thenReturn(Optional.of(productoEnDb));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ventaService.save(v));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));

        verify(productoRepository).findById(20L);
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void save_whenProductoNotFound_throwsIllegalArgumentException() {
        Venta v = new Venta();
        Usuario uRef = new Usuario();
        uRef.setId(3L);
        v.setUsuario(uRef);

        DetalleVenta det = new DetalleVenta();
        Producto pRef = new Producto();
        pRef.setId(30L);
        det.setProducto(pRef);
        det.setCantidad(1);
        v.setDetalles(List.of(det));

        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(new Usuario()));
        when(productoRepository.findById(30L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ventaService.save(v));
        assertTrue(ex.getMessage().contains("El producto indicado no existe"));

        verify(productoRepository).findById(30L);
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void save_whenUsuarioNotFound_throwsIllegalArgumentException() {
        Venta v = new Venta();
        Usuario uRef = new Usuario();
        uRef.setId(4L);
        v.setUsuario(uRef);

        DetalleVenta det = new DetalleVenta();
        Producto pRef = new Producto();
        pRef.setId(40L);
        det.setProducto(pRef);
        det.setCantidad(1);
        v.setDetalles(List.of(det));

        when(usuarioRepository.findById(4L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ventaService.save(v));
        assertTrue(ex.getMessage().contains("El usuario indicado no existe"));

        verify(usuarioRepository).findById(4L);
        verify(productoRepository, never()).findById(any());
        verify(ventaRepository, never()).save(any());
    }

    @Test
    void save_whenDetallesEmpty_throwsIllegalArgumentException() {
        Venta v = new Venta();
        v.setUsuario(new Usuario() {{ setId(5L); }});
        v.setDetalles(List.of()); // vacío

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(new Usuario()));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> ventaService.save(v));
        assertTrue(ex.getMessage().contains("La venta debe tener al menos un detalle"));

        verify(ventaRepository, never()).save(any());
    }

    @Test
    void save_nullVenta_throwsNPE() {
        assertThrows(NullPointerException.class, () -> ventaService.save(null));
    }

    @Test
    void findAll_delegates() {
        Venta a = new Venta(); a.setId(1L);
        Venta b = new Venta(); b.setId(2L);
        when(ventaRepository.findAll()).thenReturn(List.of(a, b));

        List<Venta> lista = ventaService.findAll();
        assertEquals(2, lista.size());
        verify(ventaRepository).findAll();
    }

    @Test
    void findById_returnsVenta_orNull_and_nullId_throwsNPE() {
        Venta v = new Venta(); v.setId(7L);
        when(ventaRepository.findById(7L)).thenReturn(Optional.of(v));

        Venta found = ventaService.findById(7L);
        assertNotNull(found);
        assertEquals(7L, found.getId());

        // id null -> NPE por Objects.requireNonNull
        assertThrows(NullPointerException.class, () -> ventaService.findById(null));
    }

    @Test
    void findByUsuarioId_delegates_and_null_throwsNPE() {
        when(ventaRepository.findByUsuarioId(11L)).thenReturn(List.of());

        List<Venta> res = ventaService.findByUsuarioId(11L);
        assertNotNull(res);
        verify(ventaRepository).findByUsuarioId(11L);

        assertThrows(NullPointerException.class, () -> ventaService.findByUsuarioId(null));
    }
}