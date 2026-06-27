package com.minimarket.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.Date;

class EntityTest {
    @Test
    void testGettersSetters() {
        // Test rápido de Producto
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Test");
        p.setPrecio(10.0);
        p.setStock(5);
        assertEquals(1L, p.getId());
        assertEquals("Test", p.getNombre());

        // Test rápido de Venta
        Venta v = new Venta();
        v.setId(1L);
        v.setFecha(new Date());
        v.setDetalles(new ArrayList<>());
        assertEquals(1L, v.getId());
        assertNotNull(v.getFecha());
    }
}