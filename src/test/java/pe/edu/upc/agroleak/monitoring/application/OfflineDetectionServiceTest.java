package pe.edu.upc.agroleak.monitoring.application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agroleak.devices.domain.model.*;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.alerts.domain.model.*;
import java.time.Instant;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class OfflineDetectionServiceTest {
    @Mock DeviceRepository devices; @Mock AlertService alerts;
    @Test void staleOnlineDeviceBecomesOfflineAndRaisesAlert() {
        UUID id=UUID.randomUUID(); Device d=new Device("Gateway","Ica");d.setStatus(DeviceStatus.ONLINE);d.setLastSeen(Instant.now().minusSeconds(600));
        when(devices.findStaleOnline(any())).thenReturn(List.of(id));when(devices.findLockedById(id)).thenReturn(Optional.of(d));
        new OfflineDetectionService(devices,alerts,300).check();
        assertThat(d.getStatus()).isEqualTo(DeviceStatus.OFFLINE);
        verify(alerts).createIfNotActive(eq(d),eq(AlertType.DEVICE_OFFLINE),eq(AlertSeverity.MEDIUM),anyString());
    }
    @Test void heartbeatArrivingBeforeLockPreventsFalseOfflineAlert() {
        UUID id=UUID.randomUUID();Device d=new Device("Gateway","Ica");d.setStatus(DeviceStatus.ONLINE);d.setLastSeen(Instant.now());
        when(devices.findStaleOnline(any())).thenReturn(List.of(id));when(devices.findLockedById(id)).thenReturn(Optional.of(d));
        new OfflineDetectionService(devices,alerts,300).check();verifyNoInteractions(alerts);assertThat(d.getStatus()).isEqualTo(DeviceStatus.ONLINE);
    }
}
