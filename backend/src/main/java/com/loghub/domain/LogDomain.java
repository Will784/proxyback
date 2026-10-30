package com.loghub.domain;

import com.loghub.exception.InvalidLogException;

import java.util.Locale;

/** Business area handled by one specialised microservice (each one sits behind its own proxy). */
public enum LogDomain {
    SECURITY, INGESTION, TRACE, ANALYTICS;

    public static LogDomain from(String value) {
        if (value == null) {
            throw new InvalidLogException("Debes indicar un dominio de logs.");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new InvalidLogException("Dominio de logs desconocido: " + value);
        }
    }
}
