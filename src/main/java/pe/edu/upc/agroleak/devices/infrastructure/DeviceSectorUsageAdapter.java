package pe.edu.upc.agroleak.devices.infrastructure;

import org.springframework.stereotype.Component;
import pe.edu.upc.agroleak.farm.application.SectorDeviceUsage;

import java.util.UUID;

@Component
public class DeviceSectorUsageAdapter implements SectorDeviceUsage {
    private final DeviceRepository repository;

    public DeviceSectorUsageAdapter(DeviceRepository repository) {
        this.repository = repository;
    }

    public boolean hasDevices(UUID id) {
        return repository.existsBySectorId(id);
    }
}
