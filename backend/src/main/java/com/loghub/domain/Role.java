package com.loghub.domain;

import java.util.Locale;

public enum Role {
    ADMIN, ANALYST, SERVICE, GUEST;

    /** Unknown or missing roles fall back to the least privileged one. */
    public static Role from(String value) {
        if (value == null || value.isBlank()) {
            return GUEST;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return GUEST;
        }
    }
}
