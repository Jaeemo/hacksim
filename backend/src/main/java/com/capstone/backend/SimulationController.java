package com.capstone.backend;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final SimulationService simulationService;
    private final SimulationProperties simulationProperties;
    private final DetonationAuditService auditService;

    public SimulationController(SimulationService simulationService,
                               SimulationProperties simulationProperties,
                               DetonationAuditService auditService) {
        this.simulationService = simulationService;
        this.simulationProperties = simulationProperties;
        this.auditService = auditService;
    }

    @PostMapping("/start-simulation/{simulationType}")
    public ApiResponse startSimulation(@PathVariable String simulationType, HttpServletRequest request) {
        String scenarioId = simulationType.toLowerCase(Locale.ROOT);
        String shortcutPath = simulationProperties.findShortcut(scenarioId)
                .orElseThrow(() -> new ScenarioNotFoundException(simulationType));

        DetonationRun run = auditService.start(scenarioId, request.getRemoteAddr());

        try {
            logger.info("[{}] detonation requested from {}. Reverting to clean snapshot and settling {}ms before launch.",
                    scenarioId, run.getClientIp(), simulationProperties.getStartupDelayMs());

            simulationService.runShortcut(shortcutPath, simulationProperties.getStartupDelayMs());

            auditService.markTriggered(run);
            logger.info("[{}] detonation command sent.", scenarioId);
            return ApiResponse.success(scenarioId + " simulation triggered.");
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            auditService.markFailed(run, "Simulation request interrupted.");
            throw new SimulationException("Simulation request interrupted.");
        } catch (Exception ex) {
            auditService.markFailed(run, ex.getMessage());
            throw new SimulationException("Simulation could not be started.");
        }
    }

    @GetMapping("/runs")
    public List<DetonationRunResponse> recentRuns() {
        return auditService.recentRuns();
    }
}
