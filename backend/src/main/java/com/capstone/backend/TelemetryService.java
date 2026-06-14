package com.capstone.backend;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelemetryService {

    private final DetonationRunRepository runRepository;
    private final TelemetryEventRepository eventRepository;
    private final AttackClassifier attackClassifier;

    public TelemetryService(DetonationRunRepository runRepository,
                            TelemetryEventRepository eventRepository,
                            AttackClassifier attackClassifier) {
        this.runRepository = runRepository;
        this.eventRepository = eventRepository;
        this.attackClassifier = attackClassifier;
    }

    @Transactional
    public int ingest(Long runId, List<TelemetryEventRequest> requests) {
        DetonationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RunNotFoundException(runId));

        if (requests == null || requests.isEmpty()) {
            return 0;
        }

        List<TelemetryEvent> events = new ArrayList<>(requests.size());
        for (TelemetryEventRequest request : requests) {
            TelemetryEventType type = TelemetryEventType.from(request.eventType());
            Instant occurredAt = request.occurredAt() != null ? request.occurredAt() : Instant.now();

            TelemetryEvent event = new TelemetryEvent(
                    run, type, occurredAt, request.actor(), request.target(), request.detail());
            attackClassifier.classify(type, request.actor(), request.target())
                    .ifPresent(event::applyTechnique);
            events.add(event);
        }

        eventRepository.saveAll(events);
        return events.size();
    }

    @Transactional(readOnly = true)
    public DetonationReport report(Long runId) {
        DetonationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new RunNotFoundException(runId));

        List<TelemetryEvent> events = eventRepository.findByDetonationRunIdOrderByOccurredAtAsc(runId);

        return new DetonationReport(
                run.getId(),
                run.getScenarioId(),
                run.getStatus().name(),
                events.size(),
                summariseTechniques(events),
                extractIocs(events),
                events.stream().map(TelemetryEventResponse::from).toList());
    }

    private List<DetonationReport.TechniqueSummary> summariseTechniques(List<TelemetryEvent> events) {
        Map<String, DetonationReport.TechniqueSummary> byId = new LinkedHashMap<>();
        for (TelemetryEvent event : events) {
            String id = event.getAttackTechniqueId();
            if (id == null) {
                continue;
            }
            DetonationReport.TechniqueSummary existing = byId.get(id);
            long count = existing == null ? 1 : existing.count() + 1;
            byId.put(id, new DetonationReport.TechniqueSummary(id, event.getAttackTechniqueName(), count));
        }
        return byId.values().stream()
                .sorted(Comparator.comparingLong(DetonationReport.TechniqueSummary::count).reversed())
                .toList();
    }

    private DetonationReport.Iocs extractIocs(List<TelemetryEvent> events) {
        List<String> hosts = distinctTargets(events,
                List.of(TelemetryEventType.NETWORK_CONNECT, TelemetryEventType.DNS_QUERY));
        List<String> files = distinctTargets(events,
                List.of(TelemetryEventType.FILE_WRITE, TelemetryEventType.FILE_RENAME));
        List<String> registryKeys = distinctTargets(events,
                List.of(TelemetryEventType.REGISTRY_SET));
        return new DetonationReport.Iocs(hosts, files, registryKeys);
    }

    private List<String> distinctTargets(List<TelemetryEvent> events, List<TelemetryEventType> types) {
        return events.stream()
                .filter(event -> types.contains(event.getEventType()))
                .map(TelemetryEvent::getTarget)
                .filter(target -> target != null && !target.isBlank())
                .distinct()
                .toList();
    }
}
