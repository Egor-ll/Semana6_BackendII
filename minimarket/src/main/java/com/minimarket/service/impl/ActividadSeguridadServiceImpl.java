package com.minimarket.service.impl;

import com.minimarket.entity.ActividadSeguridad;
import com.minimarket.repository.ActividadSeguridadRepository;
import com.minimarket.service.ActividadSeguridadService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
public class ActividadSeguridadServiceImpl implements ActividadSeguridadService {

    private final ActividadSeguridadRepository actividadSeguridadRepository;

    public ActividadSeguridadServiceImpl(ActividadSeguridadRepository actividadSeguridadRepository) {
        this.actividadSeguridadRepository = actividadSeguridadRepository;
    }

    @Override
    public void registrar(String username, String tipoEvento, String descripcion, String ipOrigen) {
        try {
            String usernameSeguro = sanitizarYLimitar(username, 50, null);
            String tipoEventoSeguro = sanitizarYLimitar(tipoEvento, 50, "EVENTO_NO_ESPECIFICADO");
            String descripcionSegura = sanitizarYLimitar(descripcion, 255, "Sin descripción");
            String ipOrigenSegura = sanitizarYLimitar(ipOrigen, 45, null);

            ActividadSeguridad actividad = new ActividadSeguridad(
                    usernameSeguro,
                    tipoEventoSeguro,
                    descripcionSegura,
                    ipOrigenSegura
            );

            actividadSeguridadRepository.save(actividad);
        } catch (Exception e) {
            // El registro de auditoría no debe interrumpir el flujo principal de la aplicación.
        }
    }

    private String sanitizarYLimitar(String valor, int longitudMaxima, String valorPorDefecto) {
        if (valor == null || valor.isBlank()) {
            return valorPorDefecto;
        }

        String valorSeguro = HtmlUtils.htmlEscape(valor.trim());

        if (valorSeguro.length() > longitudMaxima) {
            return valorSeguro.substring(0, longitudMaxima);
        }

        return valorSeguro;
    }
}