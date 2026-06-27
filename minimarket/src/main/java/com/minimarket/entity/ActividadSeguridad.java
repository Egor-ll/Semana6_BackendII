package com.minimarket.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "actividades_seguridad")
public class ActividadSeguridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, length = 50)
    private String tipoEvento;

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(length = 45)
    private String ipOrigen;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    public ActividadSeguridad() {
    }

    public ActividadSeguridad(String username, String tipoEvento, String descripcion, String ipOrigen) {
        this.username = username;
        this.tipoEvento = tipoEvento;
        this.descripcion = descripcion;
        this.ipOrigen = ipOrigen;
        this.fechaRegistro = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (fechaRegistro == null) {
            fechaRegistro = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getIpOrigen() {
        return ipOrigen;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
}