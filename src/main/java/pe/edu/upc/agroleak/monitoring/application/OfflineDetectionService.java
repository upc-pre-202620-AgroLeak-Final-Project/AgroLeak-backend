package pe.edu.upc.agroleak.monitoring.application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;
import pe.edu.upc.agroleak.devices.domain.model.DeviceStatus;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.alerts.domain.model.*;
import java.time.Instant;

@Service
@ConditionalOnProperty(name="agroleak.offline.enabled",havingValue="true",matchIfMissing=true)
public class OfflineDetectionService {
    private final DeviceRepository devices;
    private final AlertService alerts;
    private final long timeoutSeconds;
    public OfflineDetectionService(DeviceRepository devices,AlertService alerts,@Value("${agroleak.offline.timeout-seconds:300}") long timeoutSeconds) {
        if(timeoutSeconds<1) throw new IllegalArgumentException("offline.timeout-seconds debe ser positivo");
        this.devices=devices;this.alerts=alerts;this.timeoutSeconds=timeoutSeconds;
    }
    @Scheduled(fixedDelayString="${agroleak.offline.check-interval-ms:60000}",initialDelayString="${agroleak.offline.check-interval-ms:60000}")
    @Transactional
    public void check() {
        Instant cutoff=Instant.now().minusSeconds(timeoutSeconds);
        for(var candidate:devices.findStaleOnline(cutoff)) {
            var device=devices.findLockedById(candidate).orElseThrow();
            if(device.getStatus()!=DeviceStatus.ONLINE || device.getLastSeen()==null || !device.getLastSeen().isBefore(cutoff)) continue;
            device.setStatus(DeviceStatus.OFFLINE);
            alerts.createIfNotActive(device,AlertType.DEVICE_OFFLINE,AlertSeverity.MEDIUM,"Dispositivo sin actividad reciente");
        }
    }
}
