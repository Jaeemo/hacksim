package com.capstone.backend;

import java.time.Instant;

/**
 * One normalised telemetry event as posted by the guest agent. The ATT&CK technique is intentionally
 * not supplied by the client — the backend classifies it so the mapping cannot be spoofed.
 */
public record TelemetryEventRequest(
        String eventType,
        Instant occurredAt,
        String actor,
        String target,
        String detail) {
}
