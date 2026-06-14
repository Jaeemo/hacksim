package com.capstone.backend;

import java.time.Instant;

public record DetonationRunResponse(
        Long id,
        String scenarioId,
        String status,
        String clientIp,
        String message,
        Instant requestedAt,
        Instant completedAt) {

    public static DetonationRunResponse from(DetonationRun run) {
        return new DetonationRunResponse(
                run.getId(),
                run.getScenarioId(),
                run.getStatus().name(),
                run.getClientIp(),
                run.getMessage(),
                run.getRequestedAt(),
                run.getCompletedAt());
    }
}
