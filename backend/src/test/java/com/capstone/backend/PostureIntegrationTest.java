package com.capstone.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:posturetest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
@Transactional
class PostureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitComputesScoreFromChecks() throws Exception {
        PostureDtos.ReportRequest request = new PostureDtos.ReportRequest(
                "WIN-LAB-01", "Windows 10",
                List.of(
                        new PostureDtos.CheckRequest("firewall", "Firewall enabled", "PASS", "HIGH", "all profiles on"),
                        new PostureDtos.CheckRequest("defender", "Real-time protection", "PASS", "HIGH", "enabled"),
                        new PostureDtos.CheckRequest("updates", "Recent updates", "WARN", "MEDIUM", "no patch in 45 days"),
                        new PostureDtos.CheckRequest("guest", "Guest account disabled", "FAIL", "HIGH", "Guest is enabled")));

        // weighted = 1 + 1 + 0.5 + 0 = 2.5 over 4 -> 63
        mockMvc.perform(post("/api/posture")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(63))
                .andExpect(jsonPath("$.hostId").value("WIN-LAB-01"))
                .andExpect(jsonPath("$.checks[*].checkId", hasItem("guest")));

        mockMvc.perform(get("/api/posture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].hostId", hasItem("WIN-LAB-01")));
    }
}
