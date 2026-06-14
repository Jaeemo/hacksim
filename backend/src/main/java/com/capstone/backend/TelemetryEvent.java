package com.capstone.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A single behaviour observed in the guest during a detonation (one Sysmon-derived event), tagged
 * with the MITRE ATT&CK technique it maps to when one is recognised.
 */
@Entity
@Table(name = "telemetry_event")
public class TelemetryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private DetonationRun detonationRun;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TelemetryEventType eventType;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(length = 512)
    private String actor;

    @Column(length = 1024)
    private String target;

    @Column(length = 2000)
    private String detail;

    @Column(length = 20)
    private String attackTechniqueId;

    @Column(length = 200)
    private String attackTechniqueName;

    protected TelemetryEvent() {
    }

    public TelemetryEvent(DetonationRun detonationRun, TelemetryEventType eventType, Instant occurredAt,
                          String actor, String target, String detail) {
        this.detonationRun = detonationRun;
        this.eventType = eventType;
        this.occurredAt = occurredAt;
        this.actor = actor;
        this.target = target;
        this.detail = detail;
    }

    public void applyTechnique(AttackTechnique technique) {
        this.attackTechniqueId = technique.id();
        this.attackTechniqueName = technique.name();
    }

    public Long getId() {
        return id;
    }

    public TelemetryEventType getEventType() {
        return eventType;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getActor() {
        return actor;
    }

    public String getTarget() {
        return target;
    }

    public String getDetail() {
        return detail;
    }

    public String getAttackTechniqueId() {
        return attackTechniqueId;
    }

    public String getAttackTechniqueName() {
        return attackTechniqueName;
    }
}
