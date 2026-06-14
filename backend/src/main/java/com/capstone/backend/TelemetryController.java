package com.capstone.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/runs")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @PostMapping("/{runId}/telemetry")
    public ApiResponse ingest(@PathVariable Long runId, @RequestBody List<TelemetryEventRequest> events) {
        int ingested = telemetryService.ingest(runId, events);
        return ApiResponse.success(ingested + " telemetry events ingested.");
    }

    @GetMapping("/{runId}/report")
    public DetonationReport report(@PathVariable Long runId) {
        return telemetryService.report(runId);
    }
}
