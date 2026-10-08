package pe.edu.upc.agroleak.monitoring.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.monitoring.infrastructure.config.DetectionProperties;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorSnapshot;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorReading;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;
import pe.edu.upc.agroleak.monitoring.infrastructure.SensorReadingRepository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class DetectionService {
    private final SensorReadingRepository readingRepository;
    private final AlertService alertService;
    private final DetectionEngine engine;
    private final DetectionProperties properties;

    public DetectionService(SensorReadingRepository readingRepository, AlertService alertService,
                            DetectionEngine engine, DetectionProperties properties) {
        this.readingRepository = readingRepository;
        this.alertService = alertService;
        this.engine = engine;
        this.properties = properties;
    }

    @Transactional
    public void evaluate(Device device) {
        UUID deviceId = device.getId();
        Optional<SensorReading> flowIn = latest(deviceId, SensorType.FLOW_IN);
        Optional<SensorReading> flowOut = latest(deviceId, SensorType.FLOW_OUT);
        Optional<SensorReading> pressure = latest(deviceId, SensorType.PRESSURE);

        var anchor = java.util.stream.Stream.of(flowIn,flowOut,pressure).flatMap(Optional::stream)
                .map(SensorReading::getRecordedAt).max(java.time.Instant::compareTo).orElse(java.time.Instant.now());
        java.util.function.Function<Optional<SensorReading>,Double> fresh = reading -> reading
                .filter(r -> Math.abs(Duration.between(r.getRecordedAt(),anchor).toSeconds()) <= properties.getPairWindowSeconds())
                .map(SensorReading::getValue).orElse(null);
        SensorSnapshot snapshot = new SensorSnapshot(fresh.apply(flowIn),fresh.apply(flowOut),fresh.apply(pressure));

        engine.evaluate(snapshot).forEach(candidate ->
                alertService.createIfNotActive(device, candidate.type(), candidate.severity(), candidate.message()));
    }

    private Optional<SensorReading> latest(UUID deviceId, SensorType type) {
        return readingRepository.findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(deviceId, type);
    }
}
