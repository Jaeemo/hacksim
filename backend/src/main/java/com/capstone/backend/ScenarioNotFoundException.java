package com.capstone.backend;

public class ScenarioNotFoundException extends RuntimeException {

    public ScenarioNotFoundException(String simulationType) {
        super("Invalid simulation type: " + simulationType);
    }
}
