package com.capstone.backend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Component
@ConfigurationProperties(prefix = "simulation")
public class SimulationProperties {

    private long startupDelayMs = 5000;
    private Map<String, String> shortcuts = new LinkedHashMap<>();

    public long getStartupDelayMs() {
        return startupDelayMs;
    }

    public void setStartupDelayMs(long startupDelayMs) {
        this.startupDelayMs = startupDelayMs;
    }

    public Map<String, String> getShortcuts() {
        return shortcuts;
    }

    public void setShortcuts(Map<String, String> shortcuts) {
        this.shortcuts = shortcuts;
    }

    public Optional<String> findShortcut(String scenarioId) {
        return Optional.ofNullable(shortcuts.get(scenarioId.toLowerCase(Locale.ROOT)));
    }
}
