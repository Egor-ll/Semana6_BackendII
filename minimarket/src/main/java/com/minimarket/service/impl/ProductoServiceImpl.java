package com.minimarket.service.impl;

import com.minimarket.entity.Producto;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.service.ProductoService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoServiceImpl(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    public Producto findById(Long id) {
        return productoRepository.findById(Objects.requireNonNull(id, "El id del producto no puede ser nulo"))
                .orElse(null);
    }

    @Override
    public Producto save(Producto producto) {
        return productoRepository.save(Objects.requireNonNull(producto, "El producto no puede ser nulo"));
    }

    @Override
    public void deleteById(Long id) {
        productoRepository.deleteById(Objects.requireNonNull(id, "El id del producto no puede ser nulo"));
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        return productoRepository.findByCategoriaId(Objects.requireNonNull(categoriaId, "El id de la categoría no puede ser nulo"));
    }
}