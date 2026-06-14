package com.capstone.backend;

import java.util.Locale;

public enum CheckStatus {
    PASS,
    WARN,
    FAIL;

    public static CheckStatus from(String raw) {
        if (raw == null) {
            return WARN;
        }
        try {
            return CheckStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return WARN;
        }
    }

    public double weight() {
        return switch (this) {
            case PASS -> 1.0;
            case WARN -> 0.5;
            case FAIL -> 0.0;
        };
    }
}
