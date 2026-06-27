package com.minimarket.config;

import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository,
                           UsuarioRepository usuarioRepository,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol cliente = obtenerOCrearRol("CLIENTE");
        Rol empleado = obtenerOCrearRol("EMPLEADO");
        Rol administrador = obtenerOCrearRol("ADMINISTRADOR");

        crearUsuarioSiNoExiste("cliente", "cliente123", Set.of(cliente));
        crearUsuarioSiNoExiste("empleado", "empleado123", Set.of(empleado));
        crearUsuarioSiNoExiste("admin", "admin123", Set.of(administrador));

        Categoria abarrotes = obtenerOCrearCategoria("Abarrotes");
        Categoria bebidas = obtenerOCrearCategoria("Bebidas");
        Categoria limpieza = obtenerOCrearCategoria("Artículos de limpieza");

        crearProductoSiNoExiste("Arroz 1kg", 1590.0, 80, abarrotes);
        crearProductoSiNoExiste("Agua mineral 1.5L", 990.0, 120, bebidas);
        crearProductoSiNoExiste("Detergente líquido", 3990.0, 45, limpieza);
    }

    private Rol obtenerOCrearRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }

    private void crearUsuarioSiNoExiste(String username, String password, Set<Rol> roles) {
        if (usuarioRepository.findByUsername(username).isEmpty()) {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(password));
            usuario.setRoles(roles);
            usuarioRepository.save(usuario);
        }
    }

    private Categoria obtenerOCrearCategoria(String nombre) {
        return categoriaRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    Categoria categoria = new Categoria();
                    categoria.setNombre(nombre);
                    return categoriaRepository.save(categoria);
                });
    }

    private void crearProductoSiNoExiste(String nombre, Double precio, Integer stock, Categoria categoria) {
        if (productoRepository.findByNombreIgnoreCase(nombre).isEmpty()) {
            Producto producto = new Producto();
            producto.setNombre(nombre);
            producto.setPrecio(precio);
            producto.setStock(stock);
            producto.setCategoria(categoria);
            productoRepository.save(producto);
        }
    }
}