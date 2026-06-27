package com.minimarket.controller;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.service.DetalleVentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/detalle-ventas")
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;

    public DetalleVentaController(DetalleVentaService detalleVentaService) {
        this.detalleVentaService = detalleVentaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<DetalleVenta>> listarDetalleVentas() {
        return ResponseEntity.ok(detalleVentaService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> obtenerDetalleVentaPorId(@PathVariable Long id) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del detalle de venta debe ser válido");
        }

        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        return detalleVenta != null ? ResponseEntity.ok(detalleVenta) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> guardarDetalleVenta(@RequestBody DetalleVenta detalleVenta) {
        if (detalleVenta == null) {
            return ResponseEntity.badRequest().body("El detalle de venta no puede ser nulo");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(detalleVentaService.save(detalleVenta));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> actualizarDetalleVenta(@PathVariable Long id, @RequestBody DetalleVenta detalleVenta) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del detalle de venta debe ser válido");
        }

        if (detalleVenta == null) {
            return ResponseEntity.badRequest().body("El detalle de venta no puede ser nulo");
        }

        DetalleVenta existente = detalleVentaService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        detalleVenta.setId(id);
        return ResponseEntity.ok(detalleVentaService.save(detalleVenta));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarDetalleVenta(@PathVariable Long id) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del detalle de venta debe ser válido");
        }

        DetalleVenta detalleVenta = detalleVentaService.findById(id);
        if (detalleVenta == null) {
            return ResponseEntity.notFound().build();
        }

        detalleVentaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean idInvalido(Long id) {
        return id == null || id <= 0;
    }
}