package pe.edu.upc.agroleak.monitoring.presentation.rest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.agroleak.monitoring.application.*;
import pe.edu.upc.agroleak.monitoring.domain.model.*;
import pe.edu.upc.agroleak.monitoring.presentation.rest.dto.*;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/monitoring")
@Tag(name="Monitoring")
public class MonitoringController {
    private final ReadingQueryService queries;
    private final ReadingService readings;
    public MonitoringController(ReadingQueryService queries, ReadingService readings) { this.queries=queries; this.readings=readings; }
    @PostMapping("/readings")
    @org.springframework.web.bind.annotation.ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public ReadingResponse create(@jakarta.validation.Valid @RequestBody CreateReadingRequest r) {
        return ReadingResponse.from(readings.create(r.deviceId(),r.sensorType(),r.value(),r.unit(),r.recordedAt()));
    }
    @GetMapping("/readings")
    public List<ReadingResponse> history(@RequestParam(required=false) UUID deviceId,
            @RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to,
            @RequestParam(required=false) SensorType sensorType,@RequestParam(defaultValue="100") int limit) {
        return queries.history(deviceId,from,to,sensorType,limit).stream().map(ReadingResponse::from).toList();
    }
    @GetMapping("/devices/{deviceId}/latest")
    public ReadingResponse latest(@PathVariable UUID deviceId) { return queries.latest(deviceId).map(ReadingResponse::from).orElse(null); }
    @GetMapping("/devices/{deviceId}/latest-by-type")
    public LatestReadingsResponse byType(@PathVariable UUID deviceId) {
        Map<String,ReadingResponse> result=new LinkedHashMap<>();
        readings.latest(deviceId).forEach((type,r) -> result.put(type.name(),ReadingResponse.from(r)));
        return new LatestReadingsResponse(result);
    }
    @GetMapping("/devices/{deviceId}/summary")
    public Map<String,ReadingStatistics> summary(@PathVariable UUID deviceId,@RequestParam(required=false) Instant from,@RequestParam(required=false) Instant to) {
        var stats=queries.summary(deviceId,from,to);
        return Map.of("flowIn",stats.get(SensorType.FLOW_IN),"flowOut",stats.get(SensorType.FLOW_OUT),"pressure",stats.get(SensorType.PRESSURE),"soilMoisture",stats.get(SensorType.SOIL_MOISTURE));
    }
}
