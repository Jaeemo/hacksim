package com.capstone.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api")
public class SimulationController {

    private static final Logger logger = LoggerFactory.getLogger(SimulationController.class);

    private final SimulationService simulationService;
    private final SimulationProperties simulationProperties;

    public SimulationController(SimulationService simulationService, SimulationProperties simulationProperties) {
        this.simulationService = simulationService;
        this.simulationProperties = simulationProperties;
    }

    @PostMapping("/start-simulation/{simulationType}")
    public ResponseEntity<ApiResponse> startSimulation(@PathVariable String simulationType) {
        String scenarioId = simulationType.toLowerCase(Locale.ROOT);
        String shortcutPath = simulationProperties.findShortcut(scenarioId).orElse(null);

        if (shortcutPath == null) {
            logger.warn("Invalid simulation type requested: {}", simulationType);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Invalid simulation type: " + simulationType));
        }

        try {
            logger.info("[{}] simulation requested. Reverting to clean snapshot and settling {}ms before launch.",
                    scenarioId,
                    simulationProperties.getStartupDelayMs());

            simulationService.runShortcut(shortcutPath, simulationProperties.getStartupDelayMs());

            logger.info("[{}] simulation command sent.", scenarioId);
            return ResponseEntity.ok(ApiResponse.success(scenarioId + " simulation triggered."));
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.error("Simulation request interrupted: {}", ie.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Simulation request interrupted."));
        } catch (Exception e) {
            logger.error("Simulation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
