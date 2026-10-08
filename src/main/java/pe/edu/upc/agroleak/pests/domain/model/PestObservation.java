package pe.edu.upc.agroleak.pests.domain.model;

import jakarta.persistence.*;
import pe.edu.upc.agroleak.devices.domain.model.Device;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "pest_observations", indexes = {
        @Index(name = "idx_pest_device_recorded", columnList = "device_id,recorded_at")
})
public class PestObservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private int pestCount;

    @Column(nullable = false)
    private double confidence;

    @Column(length = 500)
    private String imageUrl;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected PestObservation() {}

    public PestObservation(Device device, int pestCount, double confidence, String imageUrl, Instant recordedAt) {
        this.device = device;
        this.pestCount = pestCount;
        this.confidence = confidence;
        this.imageUrl = imageUrl;
        this.recordedAt = recordedAt;
    }

    public UUID getId() { return id; }
    public Device getDevice() { return device; }
    public int getPestCount() { return pestCount; }
    public double getConfidence() { return confidence; }
    public String getImageUrl() { return imageUrl; }
    public Instant getRecordedAt() { return recordedAt; }
}
