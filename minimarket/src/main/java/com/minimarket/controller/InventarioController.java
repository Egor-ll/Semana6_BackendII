package com.minimarket.controller;

import com.minimarket.entity.Inventario;
import com.minimarket.service.InventarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<Inventario>> listarMovimientosDeInventario() {
        return ResponseEntity.ok(inventarioService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> obtenerMovimientoPorId(@PathVariable Long id) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del inventario debe ser válido");
        }

        Inventario inventario = inventarioService.findById(id);
        return inventario != null ? ResponseEntity.ok(inventario) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> registrarMovimiento(@RequestBody Inventario inventario) {
        if (inventario == null) {
            return ResponseEntity.badRequest().body("El inventario no puede ser nulo");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(inventarioService.save(inventario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> actualizarMovimiento(@PathVariable Long id, @RequestBody Inventario inventario) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del inventario debe ser válido");
        }

        if (inventario == null) {
            return ResponseEntity.badRequest().body("El inventario no puede ser nulo");
        }

        Inventario existente = inventarioService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        inventario.setId(id);
        return ResponseEntity.ok(inventarioService.save(inventario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarMovimiento(@PathVariable Long id) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del inventario debe ser válido");
        }

        Inventario inventario = inventarioService.findById(id);
        if (inventario == null) {
            return ResponseEntity.notFound().build();
        }

        inventarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean idInvalido(Long id) {
        return id == null || id <= 0;
    }
}