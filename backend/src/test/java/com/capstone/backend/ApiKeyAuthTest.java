package com.capstone.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:authtest;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "security.api-key=test-key"
})
@AutoConfigureMockMvc
class ApiKeyAuthTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void detonationWithoutKeyIsRejected() throws Exception {
        mockMvc.perform(post("/api/start-simulation/{type}", "ransomware"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"));
    }

    @Test
    void detonationWithWrongKeyIsRejected() throws Exception {
        mockMvc.perform(post("/api/start-simulation/{type}", "ransomware").header("X-API-Key", "nope"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validKeyPassesFilterToController() throws Exception {
        // Correct key clears the filter; an unknown scenario type then yields the controller's 404.
        mockMvc.perform(post("/api/start-simulation/{type}", "notarealtype").header("X-API-Key", "test-key"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("error"));
    }
}
