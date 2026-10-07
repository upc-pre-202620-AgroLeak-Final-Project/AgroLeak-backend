package pe.edu.upc.agroleak.device.domain;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "devices")
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeviceStatus status = DeviceStatus.OFFLINE;

    private Instant lastSeen;

    protected Device() {}

    public Device(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public DeviceStatus getStatus() { return status; }
    public Instant getLastSeen() { return lastSeen; }
    public void setName(String name) { this.name = name; }
    public void setLocation(String location) { this.location = location; }
    public void setStatus(DeviceStatus status) { this.status = status; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
}
