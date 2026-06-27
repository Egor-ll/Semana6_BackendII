package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.repository.DetalleVentaRepository;
import com.minimarket.service.DetalleVentaService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class DetalleVentaServiceImpl implements DetalleVentaService {

    private final DetalleVentaRepository detalleVentaRepository;

    public DetalleVentaServiceImpl(DetalleVentaRepository detalleVentaRepository) {
        this.detalleVentaRepository = detalleVentaRepository;
    }

    @Override
    public List<DetalleVenta> findAll() {
        return detalleVentaRepository.findAll();
    }

    @Override
    public DetalleVenta findById(Long id) {
        return detalleVentaRepository.findById(Objects.requireNonNull(id, "El id del detalle de venta no puede ser nulo"))
                .orElse(null);
    }

    @Override
    public DetalleVenta save(DetalleVenta detalleVenta) {
        return detalleVentaRepository.save(Objects.requireNonNull(detalleVenta, "El detalle de venta no puede ser nulo"));
    }

    @Override
    public void deleteById(Long id) {
        detalleVentaRepository.deleteById(Objects.requireNonNull(id, "El id del detalle de venta no puede ser nulo"));
    }

    @Override
    public List<DetalleVenta> findByVentaId(Long ventaId) {
        return detalleVentaRepository.findByVentaId(Objects.requireNonNull(ventaId, "El id de la venta no puede ser nulo"));
    }
}