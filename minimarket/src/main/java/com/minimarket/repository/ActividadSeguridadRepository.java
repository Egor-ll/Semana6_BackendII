package com.minimarket.repository;

import com.minimarket.entity.ActividadSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActividadSeguridadRepository extends JpaRepository<ActividadSeguridad, Long> {

    List<ActividadSeguridad> findByUsernameOrderByFechaRegistroDesc(String username);

    List<ActividadSeguridad> findByTipoEventoOrderByFechaRegistroDesc(String tipoEvento);

    List<ActividadSeguridad> findByFechaRegistroBetweenOrderByFechaRegistroDesc(LocalDateTime fechaInicio,
                                                                                LocalDateTime fechaFin);

    List<ActividadSeguridad> findTop50ByOrderByFechaRegistroDesc();
}