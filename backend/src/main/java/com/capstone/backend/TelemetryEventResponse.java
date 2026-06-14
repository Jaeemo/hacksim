package com.capstone.backend;

import java.time.Instant;

public record TelemetryEventResponse(
        String eventType,
        Instant occurredAt,
        String actor,
        String target,
        String detail,
        String attackTechniqueId,
        String attackTechniqueName) {

    public static TelemetryEventResponse from(TelemetryEvent event) {
        return new TelemetryEventResponse(
                event.getEventType().name(),
                event.getOccurredAt(),
                event.getActor(),
                event.getTarget(),
                event.getDetail(),
                event.getAttackTechniqueId(),
                event.getAttackTechniqueName());
    }
}
