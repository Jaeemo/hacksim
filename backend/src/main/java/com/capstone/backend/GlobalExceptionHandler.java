package com.capstone.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralises API error handling so controllers stay focused on the happy path. Detailed causes go
 * to the server log; clients receive a controlled message so internal details (e.g. raw vmrun output)
 * are not leaked, addressing the information-disclosure item in the threat model.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ScenarioNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleScenarioNotFound(ScenarioNotFoundException ex) {
        logger.warn("Scenario not found: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(RunNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse handleRunNotFound(RunNotFoundException ex) {
        logger.warn("Run not found: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(SimulationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleSimulation(SimulationException ex) {
        logger.error("Simulation error: {}", ex.getMessage());
        return ApiResponse.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse handleUnexpected(Exception ex) {
        logger.error("Unexpected error", ex);
        return ApiResponse.error("Internal server error.");
    }
}
