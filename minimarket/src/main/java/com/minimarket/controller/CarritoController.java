package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.service.CarritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<List<Carrito>> listarCarrito() {
        return ResponseEntity.ok(carritoService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> obtenerCarritoPorId(@PathVariable Long id) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del carrito debe ser válido");
        }

        Carrito carrito = carritoService.findById(id);
        return carrito != null ? ResponseEntity.ok(carrito) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<?> agregarProductoAlCarrito(@RequestBody Carrito carrito) {
        if (carrito == null) {
            return ResponseEntity.badRequest().body("El carrito no puede ser nulo");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.save(carrito));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> actualizarCarrito(@PathVariable Long id, @RequestBody Carrito carrito) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del carrito debe ser válido");
        }

        if (carrito == null) {
            return ResponseEntity.badRequest().body("El carrito no puede ser nulo");
        }

        Carrito existente = carritoService.findById(id);
        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        carrito.setId(id);
        return ResponseEntity.ok(carritoService.save(carrito));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMINISTRADOR')")
    public ResponseEntity<?> eliminarProductoDelCarrito(@PathVariable Long id) {
        if (idInvalido(id)) {
            return ResponseEntity.badRequest().body("El id del carrito debe ser válido");
        }

        Carrito carrito = carritoService.findById(id);
        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }

        carritoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean idInvalido(Long id) {
        return id == null || id <= 0;
    }
}