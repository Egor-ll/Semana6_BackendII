package com.minimarket.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Error de validación",
                errores
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarRestricciones(ConstraintViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();

        ex.getConstraintViolations().forEach(error ->
                errores.putIfAbsent(error.getPropertyPath().toString(), error.getMessage())
        );

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Parámetros inválidos",
                errores
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> manejarTipoParametroInvalido(MethodArgumentTypeMismatchException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put(ex.getName(), "El parámetro debe tener un formato válido");

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Tipo de parámetro inválido",
                errores
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("request", "El cuerpo de la petición no tiene un formato JSON válido");

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "JSON inválido",
                errores
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorIntegridad(DataIntegrityViolationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("database", "No se pudo completar la operación por una restricción de datos");

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.CONFLICT,
                "Conflicto de datos",
                errores
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(respuesta);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> manejarAccesoDenegado(AccessDeniedException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("authorization", "No tienes permisos para realizar esta operación");

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.FORBIDDEN,
                "Acceso denegado",
                errores
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respuesta);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> manejarNoAutenticado(AuthenticationException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("authentication", "Debes iniciar sesión para acceder a este recurso");

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.UNAUTHORIZED,
                "No autenticado",
                errores
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        String mensaje = ex.getMessage() != null ? ex.getMessage() : "La solicitud contiene datos inválidos";
        errores.put("request", mensaje);

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.BAD_REQUEST,
                "Solicitud inválida",
                errores
        );

        return ResponseEntity.badRequest().body(respuesta);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarErrorGeneral(Exception ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        errores.put("error", "Ocurrió un error inesperado en el servidor");

        Map<String, Object> respuesta = crearRespuesta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Error interno",
                errores
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }

    private Map<String, Object> crearRespuesta(HttpStatus status, String mensaje, Map<String, String> errores) {
        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("success", false);
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", status.value());
        respuesta.put("error", status.getReasonPhrase());
        respuesta.put("message", mensaje);
        respuesta.put("details", errores);
        return respuesta;
    }
}