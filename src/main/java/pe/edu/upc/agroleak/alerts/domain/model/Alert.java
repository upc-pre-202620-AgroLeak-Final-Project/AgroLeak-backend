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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
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

    private UUID sectorId;
    public UUID getSectorId() { return sectorId; }
    public void locate(UUID sectorId) { this.sectorId=sectorId; }
    private Instant resolvedAt;
    private Instant acknowledgedAt;
    private UUID acknowledgedBy;
    private UUID resolvedBy;
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public UUID getAcknowledgedBy() { return acknowledgedBy; }
    public UUID getResolvedBy() { return resolvedBy; }
    public void acknowledge(UUID actor) {
        if (status == AlertStatus.RESOLVED) throw new pe.edu.upc.agroleak.common.exception.BusinessRuleException("Una alerta resuelta no puede reconocerse");
        if (status == AlertStatus.ACKNOWLEDGED) return;
        status = AlertStatus.ACKNOWLEDGED; acknowledgedAt = Instant.now(); acknowledgedBy = actor;
    }
    public void resolve(UUID actor) {
        if (status == AlertStatus.RESOLVED) return;
        status = AlertStatus.RESOLVED; resolvedAt = Instant.now(); resolvedBy = actor;
    }

    protected Alert() {}

    public Alert(Device device, AlertType type, AlertSeverity severity, String message) {
        this.device = device;
        this.sectorId = device == null ? null : device.getSectorId();
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
        resolve(null);
    }
}
