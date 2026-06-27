package com.minimarket.service.impl;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(
                Objects.requireNonNull(id, "El id del usuario no puede ser nulo"));
    }

    @Override
    public Optional<Usuario> findByUsername(String username) {
        String usernameSeguro = Objects.requireNonNull(username, "El username no puede ser nulo").trim();
        return usuarioRepository.findByUsername(usernameSeguro);
    }

    @Override
    public Usuario save(Usuario usuario) {
        Usuario usuarioSeguro = Objects.requireNonNull(usuario, "El usuario no puede ser nulo");

        if (usuarioSeguro.getUsername() != null) {
            usuarioSeguro.setUsername(usuarioSeguro.getUsername().trim());
        }

        if (usuarioSeguro.getPassword() != null && !usuarioSeguro.getPassword().isBlank()) {
            if (!usuarioSeguro.getPassword().startsWith("$2a$")
                    && !usuarioSeguro.getPassword().startsWith("$2b$")
                    && !usuarioSeguro.getPassword().startsWith("$2y$")) {
                usuarioSeguro.setPassword(passwordEncoder.encode(usuarioSeguro.getPassword()));
            }
        }

        return usuarioRepository.save(usuarioSeguro);
    }

    @Override
    public void deleteById(Long id) {
        usuarioRepository.deleteById(
                Objects.requireNonNull(id, "El id del usuario no puede ser nulo"));
    }

    @Override
    public boolean validarCamposObligatorios(Usuario usuario) {
        return usuario != null
                && usuario.getUsername() != null
                && !usuario.getUsername().isBlank()
                && usuario.getPassword() != null
                && !usuario.getPassword().isBlank()
                && usuario.getRoles() != null
                && !usuario.getRoles().isEmpty();
    }

    @Override
    public boolean tienePermisoParaRegistrarVentas(Usuario usuario) {
        if (usuario == null || usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return false;
        }

        return usuario.getRoles().stream()
                .map(Rol::getNombre)
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(String::toUpperCase)
                .anyMatch(nombre ->
                        nombre.equals("ADMIN")
                                || nombre.equals("ROLE_ADMIN")
                                || nombre.equals("ADMINISTRADOR")
                                || nombre.equals("ROLE_ADMINISTRADOR")
                                || nombre.equals("VENDEDOR")
                                || nombre.equals("ROLE_VENDEDOR"));
    }
}