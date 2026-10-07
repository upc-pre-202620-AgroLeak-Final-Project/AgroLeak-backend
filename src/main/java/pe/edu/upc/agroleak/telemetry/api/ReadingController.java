package pe.edu.upc.agroleak.telemetry.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.telemetry.application.ReadingService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/readings")
public class ReadingController {
    private final ReadingService service;

    public ReadingController(ReadingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReadingResponse create(@Valid @RequestBody CreateReadingRequest request) {
        return ReadingResponse.from(service.create(request.deviceId(), request.sensorType(), request.value(), request.unit(), request.recordedAt()));
    }

    @GetMapping("/device/{deviceId}")
    public List<ReadingResponse> recent(@PathVariable UUID deviceId, @RequestParam(defaultValue = "50") int limit) {
        return service.recent(deviceId, limit).stream().map(ReadingResponse::from).toList();
    }

    @GetMapping("/device/{deviceId}/latest")
    public LatestReadingsResponse latest(@PathVariable UUID deviceId) {
        Map<String, ReadingResponse> data = new LinkedHashMap<>();
        service.latest(deviceId).forEach((type, reading) -> data.put(type.name(), ReadingResponse.from(reading)));
        return new LatestReadingsResponse(data);
    }
}
