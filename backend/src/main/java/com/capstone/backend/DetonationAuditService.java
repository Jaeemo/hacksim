package com.capstone.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DetonationAuditService {

    private final DetonationRunRepository repository;

    public DetonationAuditService(DetonationRunRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DetonationRun start(String scenarioId, String clientIp) {
        return repository.save(new DetonationRun(scenarioId, clientIp));
    }

    @Transactional
    public void markTriggered(DetonationRun run) {
        run.markTriggered();
        repository.save(run);
    }

    @Transactional
    public void markFailed(DetonationRun run, String message) {
        run.markFailed(message);
        repository.save(run);
    }

    @Transactional(readOnly = true)
    public List<DetonationRunResponse> recentRuns() {
        return repository.findTop50ByOrderByRequestedAtDesc()
                .stream()
                .map(DetonationRunResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DetonationRunResponse get(Long runId) {
        return repository.findById(runId)
                .map(DetonationRunResponse::from)
                .orElseThrow(() -> new RunNotFoundException(runId));
    }
}
