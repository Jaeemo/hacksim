package com.capstone.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Audit record of a single malware detonation request. Persisting every request gives the platform
 * non-repudiation (who triggered what, when, and how it ended), which the threat model flagged as a
 * gap, and is the foundation for the behavioural-telemetry work in the next phase.
 */
@Entity
@Table(name = "detonation_run")
public class DetonationRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String scenarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RunStatus status;

    @Column(length = 64)
    private String clientIp;

    @Column(length = 1000)
    private String message;

    @Column(nullable = false)
    private Instant requestedAt;

    private Instant completedAt;

    protected DetonationRun() {
    }

    public DetonationRun(String scenarioId, String clientIp) {
        this.scenarioId = scenarioId;
        this.clientIp = clientIp;
        this.status = RunStatus.REQUESTED;
        this.requestedAt = Instant.now();
    }

    public void markTriggered() {
        this.status = RunStatus.TRIGGERED;
        this.completedAt = Instant.now();
    }

    public void markFailed(String message) {
        this.status = RunStatus.FAILED;
        this.message = message;
        this.completedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getScenarioId() {
        return scenarioId;
    }

    public RunStatus getStatus() {
        return status;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getMessage() {
        return message;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
