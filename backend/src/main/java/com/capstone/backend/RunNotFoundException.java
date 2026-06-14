package com.capstone.backend;

public class RunNotFoundException extends RuntimeException {

    public RunNotFoundException(Long runId) {
        super("Detonation run not found: " + runId);
    }
}
