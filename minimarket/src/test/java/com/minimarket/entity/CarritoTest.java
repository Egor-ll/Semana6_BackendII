package com.minimarket.entity;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

class CarritoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        validatorFactory.close();
    }

    @Test
    void carritoValido_noDebeTenerViolaciones() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUsername("cliente01");

        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Arroz 1 kg");

        Carrito c = new Carrito();
        c.setUsuario(u);
        c.setProducto(p);
        c.setCantidad(3);

        Set<ConstraintViolation<Carrito>> violations = validator.validate(c);
        assertTrue(violations.isEmpty());
    }

    @Test
    void carritoConCantidadCero_debeDetectarViolacion() {
        Usuario u = new Usuario();
        u.setId(1L);

        Producto p = new Producto();
        p.setId(1L);

        Carrito c = new Carrito();
        c.setUsuario(u);
        c.setProducto(p);
        c.setCantidad(0); // inválido por @Min(1)

        Set<ConstraintViolation<Carrito>> violations = validator.validate(c);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "cantidad".equals(v.getPropertyPath().toString())));
    }

    @Test
    void gettersSetters_debenFuncionar() {
        Usuario u = new Usuario();
        u.setId(5L);

        Producto p = new Producto();
        p.setId(7L);

        Carrito c = new Carrito();
        c.setId(11L);
        c.setUsuario(u);
        c.setProducto(p);
        c.setCantidad(4);

        assertEquals(11L, c.getId());
        assertSame(u, c.getUsuario());
        assertSame(p, c.getProducto());
        assertEquals(4, c.getCantidad());
    }
}