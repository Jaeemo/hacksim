package com.capstone.backend;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TelemetryEventRepository extends JpaRepository<TelemetryEvent, Long> {

    List<TelemetryEvent> findByDetonationRunIdOrderByOccurredAtAsc(Long runId);
}
