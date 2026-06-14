package com.capstone.backend;

/**
 * Normalised behaviour categories the guest agent reports, decoupled from Sysmon event IDs so the
 * backend does not depend on the collection tool.
 */
public enum TelemetryEventType {
    PROCESS_CREATE,
    NETWORK_CONNECT,
    FILE_WRITE,
    FILE_RENAME,
    REGISTRY_SET,
    DNS_QUERY,
    OTHER;

    public static TelemetryEventType from(String raw) {
        if (raw == null) {
            return OTHER;
        }
        try {
            return TelemetryEventType.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
