package com.minimarket.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.CarritoService;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public List<Carrito> findAll() {
        return carritoRepository.findAll();
    }

    @Override
    public Carrito findById(Long id) {
        return carritoRepository.findById(
                Objects.requireNonNull(id, "El id no puede ser nulo"))
                .orElse(null);
    }

    @Override
    public Carrito save(Carrito carrito) {
        return carritoRepository.save(
                Objects.requireNonNull(carrito, "El carrito no puede ser nulo"));
    }

    @Override
    public void deleteById(Long id) {
        carritoRepository.deleteById(
                Objects.requireNonNull(id, "El id no puede ser nulo"));
    }

    @Override
    public List<Carrito> findByUsuarioId(Long usuarioId) {
        return carritoRepository.findByUsuarioId(
                Objects.requireNonNull(usuarioId, "El usuarioId no puede ser nulo"));
    }

    // Método agregado para pruebas de comportamiento (stock + relación Producto-Usuario)
    public Carrito agregarProducto(Long productoId, Long usuarioId, Integer cantidad) {
        Objects.requireNonNull(productoId, "El id del producto no puede ser nulo");
        Objects.requireNonNull(usuarioId, "El id del usuario no puede ser nulo");
        Objects.requireNonNull(cantidad, "La cantidad no puede ser nula");

        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        Integer stockActual = producto.getStock();
        if (stockActual == null || stockActual < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente para agregar el producto al carrito");
        }

        Carrito carrito = new Carrito();
        carrito.setProducto(producto);
        carrito.setUsuario(usuario);
        carrito.setCantidad(cantidad);

        // Reducir stock y persistir si esa es la lógica de negocio deseada
        producto.setStock(stockActual - cantidad);
        productoRepository.save(producto);

        return carritoRepository.save(carrito);
    }
}