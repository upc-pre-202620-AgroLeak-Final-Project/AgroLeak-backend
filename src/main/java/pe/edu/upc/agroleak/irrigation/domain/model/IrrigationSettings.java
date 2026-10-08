package pe.edu.upc.agroleak.irrigation.domain.model;
import jakarta.persistence.*;
import java.util.UUID;
@Entity
@Table(name="irrigation_settings")
public class IrrigationSettings {
    @Id private UUID deviceId;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private OperationMode mode;
    protected IrrigationSettings() {}
    public IrrigationSettings(UUID deviceId,OperationMode mode) { this.deviceId=deviceId;this.mode=mode; }
    public UUID getDeviceId() { return deviceId; }
    public OperationMode getMode() { return mode; }
    public void changeMode(OperationMode mode) { this.mode=mode; }
}
