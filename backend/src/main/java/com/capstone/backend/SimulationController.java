package com.capstone.backend;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api")
public class SimulationController {

    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);

    private final DetonationLauncher detonationLauncher;
    private final SimulationProperties simulationProperties;
    private final DetonationAuditService auditService;

    public SimulationController(DetonationLauncher detonationLauncher,
                               SimulationProperties simulationProperties,
                               DetonationAuditService auditService) {
        this.detonationLauncher = detonationLauncher;
        this.simulationProperties = simulationProperties;
        this.auditService = auditService;
    }

    @PostMapping("/start-simulation/{simulationType}")
    public ResponseEntity<DetonationAcceptedResponse> startSimulation(@PathVariable String simulationType,
                                                                      HttpServletRequest request) {
        String scenarioId = simulationType.toLowerCase(Locale.ROOT);
        String shortcutPath = simulationProperties.findShortcut(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(simulationType));

        DetonationRun run = auditService.start(scenarioId, request.getRemoteAddr());
        detonationLauncher.launch(run.getId(), shortcutPath, simulationProperties.getStartupDelayMs());

        logger.info("[{}] detonation accepted as run #{} from {}.", scenarioId, run.getId(), run.getClientIp());
        return ResponseEntity.accepted()
                .body(DetonationAcceptedResponse.of(run.getId(), scenarioId + " detonation started."));
    }

    @GetMapping("/runs")
    public List<DetonationRunResponse> recentRuns() {
        return auditService.recentRuns();
    }

    @GetMapping("/runs/{runId}")
    public DetonationRunResponse run(@PathVariable Long runId) {
        return auditService.get(runId);
    }
}
