package pe.edu.upc.agroleak.demo;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import pe.edu.upc.agroleak.telemetry.application.ReadingService;
import pe.edu.upc.agroleak.telemetry.domain.model.SensorType;

import java.util.UUID;

@Service
@ConditionalOnProperty(prefix = "agroleak", name = "demo-data-enabled", havingValue = "true", matchIfMissing = true)
public class DemoScenarioService {
    private final ReadingService readingService;

    public DemoScenarioService(ReadingService readingService) {
        this.readingService = readingService;
    }

    public void normal(UUID deviceId) {
        readingService.create(deviceId, SensorType.FLOW_IN, 25.0, "L/min", null);
        readingService.create(deviceId, SensorType.PRESSURE, 2.2, "bar", null);
        readingService.create(deviceId, SensorType.FLOW_OUT, 24.2, "L/min", null);
        readingService.create(deviceId, SensorType.SOIL_MOISTURE, 48.0, "%", null);
    }

    public void leak(UUID deviceId) {
        readingService.create(deviceId, SensorType.FLOW_IN, 25.0, "L/min", null);
        readingService.create(deviceId, SensorType.PRESSURE, 2.0, "bar", null);
        readingService.create(deviceId, SensorType.FLOW_OUT, 17.5, "L/min", null);
        readingService.create(deviceId, SensorType.SOIL_MOISTURE, 41.0, "%", null);
    }

    public void obstruction(UUID deviceId) {
        readingService.create(deviceId, SensorType.FLOW_IN, 20.0, "L/min", null);
        readingService.create(deviceId, SensorType.PRESSURE, 3.6, "bar", null);
        readingService.create(deviceId, SensorType.FLOW_OUT, 3.5, "L/min", null);
    }
}
