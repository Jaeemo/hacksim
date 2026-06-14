package com.capstone.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hacksimtest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class TelemetryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DetonationRunRepository runRepository;

    @Test
    void ingestThenReportClassifiesBehaviour() throws Exception {
        Long runId = runRepository.save(new DetonationRun("ransomware", "127.0.0.1")).getId();

        List<TelemetryEventRequest> events = List.of(
                new TelemetryEventRequest("FILE_RENAME", Instant.parse("2026-06-14T00:00:00Z"),
                        "C:\\Temp\\evil.exe", "C:\\docs\\report.docx.locked", null),
                new TelemetryEventRequest("REGISTRY_SET", Instant.parse("2026-06-14T00:00:01Z"),
                        "C:\\Temp\\evil.exe", "HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\evil", "C:\\Temp\\evil.exe"),
                new TelemetryEventRequest("NETWORK_CONNECT", Instant.parse("2026-06-14T00:00:02Z"),
                        "C:\\Temp\\evil.exe", "203.0.113.5:443", "tcp"));

        mockMvc.perform(post("/api/runs/{id}/telemetry", runId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(events)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        mockMvc.perform(get("/api/runs/{id}/report", runId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventCount").value(3))
                .andExpect(jsonPath("$.attackTechniques[*].id", hasItem("T1486")))
                .andExpect(jsonPath("$.attackTechniques[*].id", hasItem("T1547.001")))
                .andExpect(jsonPath("$.attackTechniques[*].id", hasItem("T1071")))
                .andExpect(jsonPath("$.iocs.files", hasItem("C:\\docs\\report.docx.locked")))
                .andExpect(jsonPath("$.iocs.registryKeys",
                        hasItem("HKLM\\Software\\Microsoft\\Windows\\CurrentVersion\\Run\\evil")))
                .andExpect(jsonPath("$.iocs.hosts", hasItem("203.0.113.5:443")));
    }

    @Test
    void reportForUnknownRunReturns404() throws Exception {
        mockMvc.perform(get("/api/runs/{id}/report", 999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void unknownSimulationTypeReturns404() throws Exception {
        mockMvc.perform(post("/api/start-simulation/{type}", "notarealtype"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void runsEndpointReturnsAuditLog() throws Exception {
        runRepository.save(new DetonationRun("worm", "10.0.0.9"));

        mockMvc.perform(get("/api/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].scenarioId", hasItem("worm")));
    }
}
