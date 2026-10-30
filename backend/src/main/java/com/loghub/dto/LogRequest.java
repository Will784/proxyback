package com.loghub.dto;

import com.loghub.domain.LogLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LogRequest(
        @NotBlank(message = "El origen es obligatorio") @Size(max = 80, message = "El origen admite máximo 80 caracteres") String source,
        @NotNull(message = "El nivel es obligatorio") LogLevel level,
        @NotBlank(message = "El mensaje es obligatorio") @Size(max = 500, message = "El mensaje admite máximo 500 caracteres") String message,
        @Size(max = 20000, message = "El payload admite máximo 20000 caracteres") String payload,
        @Size(max = 45, message = "La IP no es válida") String sourceIp) {

    public LogRequest withSourceIpIfAbsent(String ip) {
        return (sourceIp == null || sourceIp.isBlank()) ? new LogRequest(source, level, message, payload, ip) : this;
    }
}
