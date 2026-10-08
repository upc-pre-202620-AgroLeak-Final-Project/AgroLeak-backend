package pe.edu.upc.agroleak.demo.presentation.rest;

import pe.edu.upc.agroleak.demo.DemoScenarioService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/demo/scenarios")
@ConditionalOnProperty(prefix = "agroleak", name = "demo-data-enabled", havingValue = "true", matchIfMissing = true)
public class DemoScenarioController {
    private final DemoScenarioService service;

    public DemoScenarioController(DemoScenarioService service) {
        this.service = service;
    }

    @PostMapping("/normal/{deviceId}")
    public Map<String, Object> normal(@PathVariable UUID deviceId) {
        service.normal(deviceId);
        return response("NORMAL", deviceId);
    }

    @PostMapping("/leak/{deviceId}")
    public Map<String, Object> leak(@PathVariable UUID deviceId) {
        service.leak(deviceId);
        return response("LEAK", deviceId);
    }

    @PostMapping("/obstruction/{deviceId}")
    public Map<String, Object> obstruction(@PathVariable UUID deviceId) {
        service.obstruction(deviceId);
        return response("OBSTRUCTION", deviceId);
    }

    private Map<String, Object> response(String scenario, UUID deviceId) {
        return Map.of("scenario", scenario, "deviceId", deviceId, "generatedAt", Instant.now());
    }
}
