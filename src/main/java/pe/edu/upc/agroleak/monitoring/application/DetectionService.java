package pe.edu.upc.agroleak.monitoring.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.alert.application.AlertService;
import pe.edu.upc.agroleak.device.domain.Device;
import pe.edu.upc.agroleak.monitoring.config.DetectionProperties;
import pe.edu.upc.agroleak.monitoring.domain.SensorSnapshot;
import pe.edu.upc.agroleak.telemetry.domain.SensorReading;
import pe.edu.upc.agroleak.telemetry.domain.SensorType;
import pe.edu.upc.agroleak.telemetry.infrastructure.SensorReadingRepository;

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

        Double flowInValue = flowIn.map(SensorReading::getValue).orElse(null);
        Double flowOutValue = flowOut.map(SensorReading::getValue).orElse(null);

        if (flowIn.isPresent() && flowOut.isPresent()) {
            long seconds = Math.abs(Duration.between(flowIn.get().getRecordedAt(), flowOut.get().getRecordedAt()).toSeconds());
            if (seconds > properties.getPairWindowSeconds()) {
                flowInValue = null;
                flowOutValue = null;
            }
        }

        SensorSnapshot snapshot = new SensorSnapshot(
                flowInValue,
                flowOutValue,
                pressure.map(SensorReading::getValue).orElse(null));

        engine.evaluate(snapshot).forEach(candidate ->
                alertService.createIfNotActive(device, candidate.type(), candidate.severity(), candidate.message()));
    }

    private Optional<SensorReading> latest(UUID deviceId, SensorType type) {
        return readingRepository.findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(deviceId, type);
    }
}
