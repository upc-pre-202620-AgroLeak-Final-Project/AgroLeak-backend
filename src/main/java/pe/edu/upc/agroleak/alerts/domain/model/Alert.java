package pe.edu.upc.agroleak.alerts.domain.model;

import jakarta.persistence.*;
import pe.edu.upc.agroleak.devices.domain.model.Device;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts", indexes = {
        @Index(name = "idx_alert_device_status", columnList = "device_id,status")
})
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(nullable = false, length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AlertStatus status = AlertStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant resolvedAt;

    protected Alert() {}

    public Alert(Device device, AlertType type, AlertSeverity severity, String message) {
        this.device = device;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Device getDevice() { return device; }
    public AlertType getType() { return type; }
    public AlertSeverity getSeverity() { return severity; }
    public String getMessage() { return message; }
    public AlertStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }

    public void resolve() {
        this.status = AlertStatus.RESOLVED;
        this.resolvedAt = Instant.now();
    }
}
