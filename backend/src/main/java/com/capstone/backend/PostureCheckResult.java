package com.capstone.backend;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * One endpoint posture check outcome (e.g. firewall enabled, Guest account disabled). Stored as a
 * value type inside its {@link PostureReport}.
 */
@Embeddable
public class PostureCheckResult {

    @Column(length = 64)
    private String checkId;

    @Column(length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private CheckStatus status;

    @Column(length = 16)
    private String severity;

    @Column(length = 1000)
    private String detail;

    protected PostureCheckResult() {
    }

    public PostureCheckResult(String checkId, String name, CheckStatus status, String severity, String detail) {
        this.checkId = checkId;
        this.name = name;
        this.status = status;
        this.severity = severity;
        this.detail = detail;
    }

    public String getCheckId() {
        return checkId;
    }

    public String getName() {
        return name;
    }

    public CheckStatus getStatus() {
        return status;
    }

    public String getSeverity() {
        return severity;
    }

    public String getDetail() {
        return detail;
    }
}
