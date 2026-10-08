package pe.edu.upc.agroleak.irrigation.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agroleak.common.exception.BusinessRuleException;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveAction;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommand;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommandStatus;
import pe.edu.upc.agroleak.irrigation.infrastructure.ValveCommandRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValveServiceTest {
    @Mock ValveCommandRepository repository;
    @Mock DeviceService deviceService;
    @Mock pe.edu.upc.agroleak.irrigation.infrastructure.IrrigationSettingsRepository settings;
    @InjectMocks ValveService service;

    @Test
    void shouldRejectSecondPendingCommandForSameDevice() {
        UUID deviceId = UUID.randomUUID();
        Device device=new Device("Gateway", "Sector A");
        when(deviceService.lockOwned(deviceId)).thenReturn(device);
        when(deviceService.get(deviceId)).thenReturn(device);
        when(repository.existsByDeviceIdAndStatus(deviceId, ValveCommandStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.request(deviceId, ValveAction.CLOSE))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any(ValveCommand.class));
    }
}
