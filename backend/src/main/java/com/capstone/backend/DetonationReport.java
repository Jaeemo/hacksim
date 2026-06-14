package com.capstone.backend;

import java.util.List;

/**
 * Behavioural analysis report for one detonation: the observed ATT&CK techniques, extracted IOCs, and
 * the underlying event timeline. This is the analyst-facing output of the sandbox.
 */
public record DetonationReport(
        Long runId,
        String scenarioId,
        String status,
        int eventCount,
        List<TechniqueSummary> attackTechniques,
        Iocs iocs,
        List<TelemetryEventResponse> events) {

    public record TechniqueSummary(String id, String name, long count) {
    }

    public record Iocs(List<String> hosts, List<String> files, List<String> registryKeys) {
    }
}
