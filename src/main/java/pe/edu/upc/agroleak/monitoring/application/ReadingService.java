package pe.edu.upc.agroleak.monitoring.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorReading;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;
import pe.edu.upc.agroleak.monitoring.infrastructure.SensorReadingRepository;

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
        Device device = deviceService.lockOwned(deviceId);
        SensorReading reading = persist(device,sensorType,value,unit,recordedAt == null ? Instant.now() : recordedAt);
        deviceService.markSeen(deviceId);
        repository.flush();
        detectionService.evaluate(device);
        return reading;
    }

    /** One coherent acquisition, used by demo producers to avoid intermediate mixed snapshots. */
    @Transactional
    public void recordSnapshot(UUID deviceId,Map<SensorType,Double> values,Instant recordedAt) {
        Device device=deviceService.lockOwned(deviceId);
        if(values==null || values.isEmpty()) throw new IllegalArgumentException("La muestra no puede estar vacia");
        Instant at=recordedAt==null ? Instant.now() : recordedAt;
        values.forEach((type,value) -> persist(device,type,value,unit(type),at));
        deviceService.markSeen(deviceId);
        repository.flush();
        detectionService.evaluate(device);
    }
    private String unit(SensorType type) {
        if(type==null) throw new IllegalArgumentException("sensorType es obligatorio");
        return switch(type) { case FLOW_IN,FLOW_OUT -> "L/min"; case PRESSURE -> "bar"; case SOIL_MOISTURE -> "%"; };
    }
    private SensorReading persist(Device device,SensorType type,double value,String unit,Instant at) {
        if (!Double.isFinite(value) || value < 0 || (type == SensorType.SOIL_MOISTURE && value > 100))
            throw new IllegalArgumentException("Valor de sensor fuera de rango");
        String expected=unit(type);
        if (!expected.equals(unit)) throw new IllegalArgumentException("Unidad esperada: " + expected);
        if (at.isAfter(Instant.now())) throw new IllegalArgumentException("recordedAt no puede ser futuro");
        return repository.save(new SensorReading(device,type,value,unit,at));
    }

    @Transactional(readOnly = true)
    public List<SensorReading> recent(UUID deviceId, int limit) {
        deviceService.get(deviceId);
        int safeLimit = pe.edu.upc.agroleak.common.domain.TimeRange.limit(limit);
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
        deviceService.get(deviceId);
        return repository.findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(deviceId, type);
    }
}
