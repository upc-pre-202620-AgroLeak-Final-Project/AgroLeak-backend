package pe.edu.upc.agroleak.telemetry.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.telemetry.domain.model.SensorReading;
import pe.edu.upc.agroleak.telemetry.domain.model.SensorType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID> {
    Optional<SensorReading> findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(UUID deviceId, SensorType sensorType);
    List<SensorReading> findByDeviceIdOrderByRecordedAtDesc(UUID deviceId, Pageable pageable);
}
