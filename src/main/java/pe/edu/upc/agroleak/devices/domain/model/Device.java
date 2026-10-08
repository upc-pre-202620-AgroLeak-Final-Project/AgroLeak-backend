package pe.edu.upc.agroleak.devices.domain.model;

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

    // Nullable additions preserve existing device records.
    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DeviceType deviceType;
    private Integer batteryLevel;
    @Column(length = 100)
    private String firmwareVersion;
    private java.time.LocalDate installationDate;
    private UUID sectorId;
    private UUID ownerId;

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public java.time.LocalDate getInstallationDate() {
        return installationDate;
    }

    public UUID getSectorId() {
        return sectorId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void assignSector(UUID sectorId) {
        this.sectorId = sectorId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void configure(DeviceType type, Integer battery, String firmware, java.time.LocalDate installed) {
        this.deviceType = type;
        this.batteryLevel = battery;
        this.firmwareVersion = firmware;
        this.installationDate = installed;
    }

    protected Device() {
    }

    public Device(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public DeviceStatus getStatus() {
        return status;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setStatus(DeviceStatus status) {
        this.status = status;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
