package com.capstone.backend;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Endpoint posture self-assessment submitted by the diagnostic agent. The score is derived from the
 * checks (PASS = 1.0, WARN = 0.5, FAIL = 0.0) so it cannot drift from the underlying findings.
 */
@Entity
@Table(name = "posture_report")
public class PostureReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String hostId;

    @Column(length = 200)
    private String os;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private Instant reportedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "posture_check", joinColumns = @JoinColumn(name = "report_id"))
    private List<PostureCheckResult> checks = new ArrayList<>();

    protected PostureReport() {
    }

    public PostureReport(String hostId, String os, List<PostureCheckResult> checks) {
        this.hostId = hostId;
        this.os = os;
        this.checks = checks != null ? checks : new ArrayList<>();
        this.score = computeScore(this.checks);
        this.reportedAt = Instant.now();
    }

    private static int computeScore(List<PostureCheckResult> checks) {
        if (checks.isEmpty()) {
            return 0;
        }
        double weighted = checks.stream().mapToDouble(check -> check.getStatus().weight()).sum();
        return (int) Math.round(100.0 * weighted / checks.size());
    }

    public Long getId() {
        return id;
    }

    public String getHostId() {
        return hostId;
    }

    public String getOs() {
        return os;
    }

    public int getScore() {
        return score;
    }

    public Instant getReportedAt() {
        return reportedAt;
    }

    public List<PostureCheckResult> getChecks() {
        return checks;
    }
}
