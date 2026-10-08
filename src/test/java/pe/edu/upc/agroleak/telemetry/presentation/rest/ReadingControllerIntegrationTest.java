package pe.edu.upc.agroleak.telemetry.presentation.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.telemetry.domain.model.SensorType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReadingControllerIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired DeviceService deviceService;

    @Test
    void shouldCreateLeakAlertAfterInconsistentFlowReadings() throws Exception {
        Device device = deviceService.create("Integration Gateway", "Test Sector");

        postReading(device, SensorType.FLOW_IN, 25.0, "L/min");
        postReading(device, SensorType.PRESSURE, 2.0, "bar");
        postReading(device, SensorType.FLOW_OUT, 17.0, "L/min");

        mockMvc.perform(get("/api/v1/alerts")
                        .param("deviceId", device.getId().toString())
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("LEAK"));
    }

    private void postReading(Device device, SensorType type, double value, String unit) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "deviceId", device.getId(),
                "sensorType", type.name(),
                "value", value,
                "unit", unit));

        mockMvc.perform(post("/api/v1/readings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
