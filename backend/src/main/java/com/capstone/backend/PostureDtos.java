package com.capstone.backend;

import java.time.Instant;
import java.util.List;

/**
 * Request/response payloads for the endpoint posture API, grouped together since they are small and
 * always used as a set.
 */
public final class PostureDtos {

    private PostureDtos() {
    }

    public record CheckRequest(String checkId, String name, String status, String severity, String detail) {
    }

    public record ReportRequest(String hostId, String os, List<CheckRequest> checks) {
    }

    public record CheckResponse(String checkId, String name, String status, String severity, String detail) {

        public static CheckResponse from(PostureCheckResult check) {
            return new CheckResponse(check.getCheckId(), check.getName(), check.getStatus().name(),
                    check.getSeverity(), check.getDetail());
        }
    }

    public record ReportResponse(Long id, String hostId, String os, int score, Instant reportedAt,
                                 List<CheckResponse> checks) {

        public static ReportResponse from(PostureReport report) {
            return new ReportResponse(report.getId(), report.getHostId(), report.getOs(), report.getScore(),
                    report.getReportedAt(),
                    report.getChecks().stream().map(CheckResponse::from).toList());
        }
    }
}
