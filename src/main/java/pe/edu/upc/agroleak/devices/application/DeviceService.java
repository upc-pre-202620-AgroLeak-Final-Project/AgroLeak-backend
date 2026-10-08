package pe.edu.upc.agroleak.devices.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.devices.domain.model.DeviceStatus;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DeviceService {
    private final DeviceRepository repository;

    public DeviceService(DeviceRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Device create(String name, String location) {
        return repository.save(new Device(name, location));
    }

    @Transactional(readOnly = true)
    public Device get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public List<Device> findAll() {
        return repository.findAll();
    }

    @Transactional
    public void markSeen(UUID deviceId) {
        Device device = get(deviceId);
        device.setLastSeen(Instant.now());
        device.setStatus(DeviceStatus.ONLINE);
    }
}
