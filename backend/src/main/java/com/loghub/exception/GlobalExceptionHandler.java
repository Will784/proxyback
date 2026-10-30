package com.loghub.exception;

import com.loghub.dto.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(LogHubException.class)
    public ResponseEntity<ApiError> handleDomain(LogHubException ex) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(ex.status());
        Object retryAfter = ex.details().get("retryAfterSeconds");
        if (retryAfter != null) {
            builder.header(HttpHeaders.RETRY_AFTER, retryAfter.toString());
        }
        return builder.body(error(ex.status(), ex.code(), ex.getMessage(), ex.details()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("Los datos enviados no son válidos.");
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> handleMalformed(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "La petición no tiene un formato válido.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex) {
        LOG.error("Unexpected failure", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Ocurrió un error inesperado. Intenta de nuevo.");
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(error(status, code, message, Map.of()));
    }

    private ApiError error(HttpStatus status, String code, String message, Map<String, Object> details) {
        return new ApiError(Instant.now(), status.value(), code, message, details);
    }
}
