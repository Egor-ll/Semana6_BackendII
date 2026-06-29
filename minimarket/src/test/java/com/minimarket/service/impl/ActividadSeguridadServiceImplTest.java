package com.minimarket.service.impl;

import com.minimarket.entity.ActividadSeguridad;
import com.minimarket.repository.ActividadSeguridadRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.HtmlUtils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActividadSeguridadServiceImplTest {

    @Mock
    private ActividadSeguridadRepository actividadSeguridadRepository;

    @InjectMocks
    private ActividadSeguridadServiceImpl service;

    @Test
    void registrar_normalInputs_savesSanitizedActividad() {
        String username = "juan";
        String tipoEvento = "LOGIN";
        String descripcion = "Usuario inicio sesion"; // Evito tildes para simplificar o uso escape
        String ip = "192.168.0.1";

        service.registrar(username, tipoEvento, descripcion, ip);

        ArgumentCaptor<ActividadSeguridad> captor = ArgumentCaptor.forClass(ActividadSeguridad.class);
        verify(actividadSeguridadRepository).save(captor.capture());

        ActividadSeguridad saved = captor.getValue();
        assertEquals("juan", saved.getUsername());
        assertEquals("LOGIN", saved.getTipoEvento());
        // Validamos que el resultado coincida con lo que HtmlUtils genera
        assertEquals(HtmlUtils.htmlEscape(descripcion), saved.getDescripcion());
        assertEquals("192.168.0.1", saved.getIpOrigen());
    }

    @Test
    void registrar_nullAndBlankInputs_useDefaultsOrNulls() {
        service.registrar(null, null, "   ", null);

        ArgumentCaptor<ActividadSeguridad> captor = ArgumentCaptor.forClass(ActividadSeguridad.class);
        verify(actividadSeguridadRepository).save(captor.capture());

        ActividadSeguridad saved = captor.getValue();
        assertNull(saved.getUsername());
        assertEquals("EVENTO_NO_ESPECIFICADO", saved.getTipoEvento());
        assertEquals("Sin descripción", saved.getDescripcion());
        assertNull(saved.getIpOrigen());
    }

    @Test
    void registrar_truncatesAndEscapesHtml() {
        // String largo de 60 caracteres
        String longUsername = IntStream.range(0, 60).mapToObj(i -> "u").collect(Collectors.joining());
        String htmlTipo = "<script>alert(1)</script>";
        
        service.registrar(longUsername, htmlTipo, "Desc", "10.0.0.5");

        ArgumentCaptor<ActividadSeguridad> captor = ArgumentCaptor.forClass(ActividadSeguridad.class);
        verify(actividadSeguridadRepository).save(captor.capture());
        ActividadSeguridad saved = captor.getValue();

        // Verificamos que no exceda los limites definidos en el service
        assertTrue(saved.getUsername().length() <= 50);
        assertTrue(saved.getTipoEvento().length() <= 50);
        // Verificamos que el HTML esté escapado (los tags < > se convierten en &lt; &gt;)
        assertTrue(saved.getTipoEvento().contains("&lt;script&gt;"));
    }

    @Test
    void registrar_repositoryThrows_exceptionIsSwallowed() {
        doThrow(new RuntimeException("Error de DB")).when(actividadSeguridadRepository).save(any());

        // No debe lanzar excepción hacia arriba (el catch del service la atrapa)
        assertDoesNotThrow(() -> service.registrar("user", "EV", "desc", "127.0.0.1"));

        verify(actividadSeguridadRepository).save(any(ActividadSeguridad.class));
    }
}