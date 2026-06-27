package com.minimarket.service.impl;

import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.service.CategoriaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImpl(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> findAll() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria findById(Long id) {
        return categoriaRepository.findById(Objects.requireNonNull(id, "El id de la categoría no puede ser nulo"))
                .orElse(null);
    }

    @Override
    public Categoria save(Categoria categoria) {
        return categoriaRepository.save(Objects.requireNonNull(categoria, "La categoría no puede ser nula"));
    }

    @Override
    public void deleteById(Long id) {
        categoriaRepository.deleteById(Objects.requireNonNull(id, "El id de la categoría no puede ser nulo"));
    }
}