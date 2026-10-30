package com.loghub.proxy;

import com.loghub.dto.LogRequest;
import com.loghub.exception.InvalidLogException;

import java.util.regex.Pattern;

/** Business-level validation applied at the ingestion gate (stricter than the transport DTO rules). */
public final class LogValidator {

    private static final Pattern SOURCE = Pattern.compile("^[A-Za-z0-9._-]{1,80}$");
    private static final Pattern CONTROL_CHARS = Pattern.compile("\\p{Cntrl}");
    private static final int MAX_PAYLOAD = 5_000;

    public void validate(LogRequest request) {
        if (request.level() == null) {
            throw new InvalidLogException("El nivel es obligatorio.");
        }
        if (request.source() == null || !SOURCE.matcher(request.source().trim()).matches()) {
            throw new InvalidLogException("El origen solo admite letras, números, punto, guion y guion bajo.");
        }
        if (request.message() == null || request.message().isBlank()) {
            throw new InvalidLogException("El mensaje no puede estar vacío.");
        }
        if (CONTROL_CHARS.matcher(request.message()).find()) {
            throw new InvalidLogException("El mensaje no puede contener saltos de línea ni caracteres de control.");
        }
        if (request.payload() != null && request.payload().length() > MAX_PAYLOAD) {
            throw new InvalidLogException("En la ingesta el payload admite máximo " + MAX_PAYLOAD
                    + " caracteres; usa el servicio de trazas para cargas pesadas.");
        }
    }
}
