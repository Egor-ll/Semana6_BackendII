package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.VentaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(Objects.requireNonNull(id, "El id de la venta no puede ser nulo"))
                .orElse(null);
    }

    @Override
    @Transactional
    public Venta save(Venta venta) {
        Venta ventaSegura = Objects.requireNonNull(venta, "La venta no puede ser nula");

        if (ventaSegura.getUsuario() == null || ventaSegura.getUsuario().getId() == null) {
            throw new IllegalArgumentException("La venta debe tener un usuario válido");
        }

        Usuario usuario = usuarioRepository.findById(ventaSegura.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("El usuario indicado no existe"));

        if (ventaSegura.getDetalles() == null || ventaSegura.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }

        if (ventaSegura.getFecha() == null) {
            ventaSegura.setFecha(new Date());
        }

        ventaSegura.setUsuario(usuario);

        for (DetalleVenta detalle : ventaSegura.getDetalles()) {
            if (detalle == null) {
                throw new IllegalArgumentException("Los detalles de la venta no pueden contener valores nulos");
            }

            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                throw new IllegalArgumentException("Cada detalle debe tener un producto válido");
            }

            if (detalle.getCantidad() == null || detalle.getCantidad() < 1) {
                throw new IllegalArgumentException("La cantidad de cada detalle debe ser mayor o igual a 1");
            }

            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new IllegalArgumentException("El producto indicado no existe"));

            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            producto.setStock(producto.getStock() - detalle.getCantidad());

            detalle.setProducto(producto);
            detalle.setPrecio(producto.getPrecio());
            detalle.setVenta(ventaSegura);
        }

        return ventaRepository.save(ventaSegura);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(Objects.requireNonNull(usuarioId, "El id del usuario no puede ser nulo"));
    }
}