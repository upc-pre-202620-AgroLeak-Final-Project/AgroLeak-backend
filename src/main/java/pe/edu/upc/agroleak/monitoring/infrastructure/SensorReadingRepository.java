package pe.edu.upc.agroleak.monitoring.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorReading;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingRepository extends JpaRepository<SensorReading, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<SensorReading> {
    Optional<SensorReading> findTopByDeviceIdAndSensorTypeOrderByRecordedAtDesc(UUID deviceId, SensorType sensorType);
    List<SensorReading> findByDeviceIdOrderByRecordedAtDesc(UUID deviceId, Pageable pageable);
    Optional<SensorReading> findTopByDeviceIdOrderByRecordedAtDesc(UUID deviceId);
    Optional<SensorReading> findTopByDeviceIdAndSensorTypeAndRecordedAtLessThanOrderByRecordedAtDesc(UUID deviceId, SensorType type, java.time.Instant before);
    @org.springframework.data.jpa.repository.Query("select r.sensorType as sensorType, avg(r.value) as average, min(r.value) as min, max(r.value) as max, count(r) as samples from SensorReading r where r.device.id = :deviceId and r.recordedAt >= :from and r.recordedAt < :to group by r.sensorType")
    List<SensorAggregate> summarize(UUID deviceId, java.time.Instant from, java.time.Instant to);
    interface SensorAggregate {
        SensorType getSensorType(); Double getAverage(); Double getMin(); Double getMax(); Long getSamples();
    }

}
