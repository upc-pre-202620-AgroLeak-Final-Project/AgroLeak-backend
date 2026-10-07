package pe.edu.upc.agroleak.valve.domain;

import jakarta.persistence.*;
import pe.edu.upc.agroleak.device.domain.Device;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "valve_commands", indexes = {
        @Index(name = "idx_valve_device_requested", columnList = "device_id,requested_at")
})
public class ValveCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ValveAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ValveCommandStatus status = ValveCommandStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    private Instant confirmedAt;

    protected ValveCommand() {}

    public ValveCommand(Device device, ValveAction action) {
        this.device = device;
        this.action = action;
        this.requestedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public Device getDevice() { return device; }
    public ValveAction getAction() { return action; }
    public ValveCommandStatus getStatus() { return status; }
    public Instant getRequestedAt() { return requestedAt; }
    public Instant getConfirmedAt() { return confirmedAt; }

    public void confirm(boolean success) {
        this.status = success ? ValveCommandStatus.CONFIRMED : ValveCommandStatus.FAILED;
        this.confirmedAt = Instant.now();
    }
}
