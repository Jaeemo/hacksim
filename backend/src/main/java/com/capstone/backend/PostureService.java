package com.capstone.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostureService {

    private final PostureReportRepository repository;

    public PostureService(PostureReportRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PostureDtos.ReportResponse ingest(PostureDtos.ReportRequest request) {
        List<PostureCheckResult> checks = request.checks() == null ? List.of() : request.checks().stream()
                .map(check -> new PostureCheckResult(
                        check.checkId(),
                        check.name(),
                        CheckStatus.from(check.status()),
                        check.severity(),
                        check.detail()))
                .toList();

        PostureReport report = repository.save(new PostureReport(request.hostId(), request.os(), checks));
        return PostureDtos.ReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<PostureDtos.ReportResponse> recent() {
        return repository.findTop20ByOrderByReportedAtDesc()
                .stream()
                .map(PostureDtos.ReportResponse::from)
                .toList();
    }
}
