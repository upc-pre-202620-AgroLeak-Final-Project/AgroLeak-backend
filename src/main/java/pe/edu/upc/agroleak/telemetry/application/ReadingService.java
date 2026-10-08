package pe.edu.upc.agroleak.telemetry.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.monitoring.application.DetectionService;
import pe.edu.upc.agroleak.telemetry.domain.model.SensorReading;
import pe.edu.upc.agroleak.telemetry.domain.model.SensorType;
import pe.edu.upc.agroleak.telemetry.infrastructure.SensorReadingRepository;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReadingService {
    private final SensorReadingRepository repository;
    private final DeviceService deviceService;
    private final DetectionService detectionService;

    public ReadingService(SensorReadingRepository repository, DeviceService deviceService, DetectionService detectionService) {
        this.repository = repository;
        this.deviceService = deviceService;
        this.detectionService = detectionService;
    }

    @Transactional
    public SensorReading create(UUID deviceId, SensorType sensorType, double value, String unit, Instant recordedAt) {
        Device device = deviceService.get(deviceId);
        Instant timestamp = recordedAt == null ? Instant.now() : recordedAt;
        SensorReading reading = repository.save(new SensorReading(device, sensorType, value, unit, timestamp));
        deviceService.markSeen(deviceId);
        repository.flush();
        detectionService.evaluate(device);
        return reading;
    }

    @Transactional(readOnly = true)
    public List<SensorReading> recent(UUID deviceId, int limit) {
        deviceService.get(deviceId);
        int safeLimit = Math.max(1, Math.min(limit, 500));
        return repository.findByDeviceIdOrderByRecordedAtDesc(deviceId, PageRequest.of(0, safeLimit));
    }

    @Transactional(readOnly = true)
    public Map<SensorType, SensorReading> latest(UUID deviceId) {
        deviceService.get(deviceId);
        Map<SensorType, SensorReading> result = new EnumMap<>(SensorType.class);
        for (SensorType type : SensorType.values()) {
            repository.findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(deviceId, type)
                    .ifPresent(reading -> result.put(type, reading));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Optional<SensorReading> latest(UUID deviceId, SensorType type) {
        return repository.findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(deviceId, type);
    }
}
