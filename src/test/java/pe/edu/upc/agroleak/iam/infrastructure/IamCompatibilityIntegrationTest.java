package pe.edu.upc.agroleak.iam.infrastructure;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IamCompatibilityIntegrationTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldKeepExistingOpenApiPathsAndExposeSecureIam() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode document = objectMapper.readTree(body);
        Set<String> paths = new HashSet<>();
        document.path("paths").fieldNames().forEachRemaining(paths::add);
        assertThat(paths).contains(
                "/api/v1/devices", "/api/v1/devices/{id}",
                "/api/v1/readings", "/api/v1/readings/device/{deviceId}",
                "/api/v1/readings/device/{deviceId}/latest",
                "/api/v1/alerts", "/api/v1/alerts/{id}/resolve",
                "/api/v1/valves/{deviceId}/commands", "/api/v1/valves/commands/{commandId}/confirm",
                "/api/v1/valves/{deviceId}/latest", "/api/v1/pest-observations",
                "/api/v1/pest-observations/device/{deviceId}", "/api/v1/dashboard/{deviceId}");
        assertThat(document.path("components").path("schemas").has("User")).isFalse();
        assertThat(paths).contains("/api/v1/iam/auth/register", "/api/v1/iam/auth/login", "/api/v1/iam/users/me", "/api/v1/farms");
        assertThat(document.path("components").path("securitySchemes").has("bearerAuth")).isTrue();
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
