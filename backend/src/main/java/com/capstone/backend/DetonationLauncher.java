package com.capstone.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Performs the actual detonation (snapshot revert + settle + launch) on a background thread so the
 * HTTP caller gets an immediate 202. Status transitions land in the audit log, which the dashboard
 * polls.
 */
@Service
public class DetonationLauncher {

    private static final Logger logger = LoggerFactory.getLogger(DetonationLauncher.class);

    private final SimulationService simulationService;
    private final DetonationAuditService auditService;
    private final DetonationRunRepository runRepository;

    public DetonationLauncher(SimulationService simulationService,
                              DetonationAuditService auditService,
                              DetonationRunRepository runRepository) {
        this.simulationService = simulationService;
        this.auditService = auditService;
        this.runRepository = runRepository;
    }

    @Async("detonationExecutor")
    public void launch(Long runId, String shortcutPath, long settleDelayMs) {
        DetonationRun run = runRepository.findById(runId).orElse(null);
        if (run == null) {
            logger.warn("Detonation worker could not find run #{}", runId);
            return;
        }

        try {
            simulationService.runShortcut(shortcutPath, settleDelayMs);
            auditService.markTriggered(run);
            logger.info("[{}] detonation run #{} triggered.", run.getScenarioId(), runId);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            auditService.markFailed(run, "Detonation interrupted.");
            logger.error("Detonation run #{} interrupted.", runId);
        } catch (Exception ex) {
            auditService.markFailed(run, ex.getMessage());
            logger.error("Detonation run #{} failed: {}", runId, ex.getMessage());
        }
    }
}
