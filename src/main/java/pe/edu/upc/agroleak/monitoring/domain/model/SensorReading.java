package pe.edu.upc.agroleak.monitoring.domain.model;

import jakarta.persistence.*;
import pe.edu.upc.agroleak.devices.domain.model.Device;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sensor_readings", indexes = {
        @Index(name = "idx_reading_device_sensor_time", columnList = "device_id,sensor_type,recorded_at")
})
public class SensorReading {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_type", nullable = false, length = 30)
    private SensorType sensorType;

    @Column(nullable = false)
    private double value;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected SensorReading() {}

    public SensorReading(Device device, SensorType sensorType, double value, String unit, Instant recordedAt) {
        this.device = device;
        this.sensorType = sensorType;
        this.value = value;
        this.unit = unit;
        this.recordedAt = recordedAt;
    }

    public UUID getId() { return id; }
    public Device getDevice() { return device; }
    public SensorType getSensorType() { return sensorType; }
    public double getValue() { return value; }
    public String getUnit() { return unit; }
    public Instant getRecordedAt() { return recordedAt; }
}
