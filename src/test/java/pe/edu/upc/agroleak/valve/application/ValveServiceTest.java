package pe.edu.upc.agroleak.valve.application;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upc.agroleak.common.exception.BusinessRuleException;
import pe.edu.upc.agroleak.device.application.DeviceService;
import pe.edu.upc.agroleak.device.domain.Device;
import pe.edu.upc.agroleak.valve.domain.ValveAction;
import pe.edu.upc.agroleak.valve.domain.ValveCommand;
import pe.edu.upc.agroleak.valve.domain.ValveCommandStatus;
import pe.edu.upc.agroleak.valve.infrastructure.ValveCommandRepository;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValveServiceTest {
    @Mock ValveCommandRepository repository;
    @Mock DeviceService deviceService;
    @InjectMocks ValveService service;

    @Test
    void shouldRejectSecondPendingCommandForSameDevice() {
        UUID deviceId = UUID.randomUUID();
        when(deviceService.get(deviceId)).thenReturn(new Device("Gateway", "Sector A"));
        when(repository.existsByDeviceIdAndStatus(deviceId, ValveCommandStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> service.request(deviceId, ValveAction.CLOSE))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any(ValveCommand.class));
    }
}
