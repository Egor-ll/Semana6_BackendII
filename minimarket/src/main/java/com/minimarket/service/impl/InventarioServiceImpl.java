package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;

    public InventarioServiceImpl(InventarioRepository inventarioRepository) {
        this.inventarioRepository = inventarioRepository;
    }

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(Objects.requireNonNull(id, "El id del inventario no puede ser nulo"))
                .orElse(null);
    }

    @Override
    public Inventario save(Inventario inventario) {
        return inventarioRepository.save(Objects.requireNonNull(inventario, "El inventario no puede ser nulo"));
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(Objects.requireNonNull(id, "El id del inventario no puede ser nulo"));
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(Objects.requireNonNull(productoId, "El id del producto no puede ser nulo"));
    }
}