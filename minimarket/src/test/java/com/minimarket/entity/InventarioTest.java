package com.minimarket.entity;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Date;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

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
    void inventarioValido_noDebeTenerViolaciones() {
        Producto p = new Producto();
        p.setId(1L);
        p.setNombre("Azúcar 1 kg");

        Inventario inv = new Inventario();
        inv.setProducto(p);
        inv.setCantidad(20);
        inv.setTipoMovimiento("Entrada");
        inv.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> violations = validator.validate(inv);
        assertTrue(violations.isEmpty(), () -> "No deberían existir violaciones, pero se encontraron: " + violations);
    }

    @Test
    void tipoMovimientoVacio_debeDetectarViolacion() {
        Producto p = new Producto();
        p.setId(1L);

        Inventario inv = new Inventario();
        inv.setProducto(p);
        inv.setCantidad(5);
        inv.setTipoMovimiento("   "); // NotBlank should consider this invalid
        inv.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> violations = validator.validate(inv);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "tipoMovimiento".equals(v.getPropertyPath().toString())),
                "Se esperaba una violación sobre 'tipoMovimiento'");
    }

    @Test
    void cantidadCero_oMenorQueUno_debeDetectarViolacion() {
        Producto p = new Producto();
        p.setId(1L);

        Inventario inv = new Inventario();
        inv.setProducto(p);
        inv.setCantidad(0); // inválido por @Min(1)
        inv.setTipoMovimiento("Entrada");
        inv.setFechaMovimiento(new Date());

        Set<ConstraintViolation<Inventario>> violations = validator.validate(inv);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "cantidad".equals(v.getPropertyPath().toString())),
                "Se esperaba una violación sobre 'cantidad'");

        // también probar null
        inv.setCantidad(null);
        violations = validator.validate(inv);
        assertTrue(violations.stream().anyMatch(v -> "cantidad".equals(v.getPropertyPath().toString())),
                "Se esperaba una violación sobre 'cantidad' cuando es null");
    }

    @Test
    void fechaMovimientoNula_debeDetectarViolacion() {
        Producto p = new Producto();
        p.setId(1L);

        Inventario inv = new Inventario();
        inv.setProducto(p);
        inv.setCantidad(3);
        inv.setTipoMovimiento("Salida");
        inv.setFechaMovimiento(null); // inválido por @NotNull

        Set<ConstraintViolation<Inventario>> violations = validator.validate(inv);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "fechaMovimiento".equals(v.getPropertyPath().toString())),
                "Se esperaba una violación sobre 'fechaMovimiento'");
    }

    @Test
    void gettersYSettersFuncionan() {
        Producto p = new Producto();
        p.setId(7L);
        p.setNombre("Aceite vegetal");

        Date ahora = new Date();

        Inventario inv = new Inventario();
        inv.setId(33L);
        inv.setProducto(p);
        inv.setCantidad(12);
        inv.setTipoMovimiento("Entrada");
        inv.setFechaMovimiento(ahora);

        assertEquals(33L, inv.getId());
        assertSame(p, inv.getProducto());
        assertEquals(12, inv.getCantidad());
        assertEquals("Entrada", inv.getTipoMovimiento());
        assertEquals(ahora, inv.getFechaMovimiento());
    }
}