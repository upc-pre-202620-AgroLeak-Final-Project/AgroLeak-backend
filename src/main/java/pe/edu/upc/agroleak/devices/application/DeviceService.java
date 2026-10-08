package pe.edu.upc.agroleak.devices.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.devices.domain.model.*;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;
import pe.edu.upc.agroleak.farm.application.FarmAccessService;
import pe.edu.upc.agroleak.iam.application.CurrentUser;

import java.time.*;
import java.util.*;

@Service
public class DeviceService {
    private final DeviceRepository repository;
    private final FarmAccessService farms;
    private final CurrentUser current;

    public DeviceService(DeviceRepository repository, FarmAccessService farms, CurrentUser current) {
        this.repository = repository;
        this.farms = farms;
        this.current = current;
    }

    @Transactional
    public Device create(String name, String location) {
        return create(name, location, null, null, null, null, null, null);
    }

    @Transactional
    public Device create(String name, String location, DeviceType type, DeviceStatus status, Integer battery,
                         String firmware, LocalDate installed, UUID sectorId) {
        farms.requireManager();
        if (sectorId != null) farms.requireSector(sectorId);
        var device = new Device(name, location);
        device.setOwnerId(current.id());
        device.configure(type, battery, firmware, installed);
        if (status != null) device.setStatus(status);
        device.assignSector(sectorId);
        return repository.save(device);
    }

    private boolean canRead(Device device) {
        if (current.isAdmin() || current.isTechnician()) return true;
        if (device.getSectorId() != null) return farms.ownsSector(device.getSectorId());
        return current.id().equals(device.getOwnerId());
    }

    @Transactional(readOnly = true)
    public Device get(UUID id) {
        var device = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + id));
        if (!canRead(device)) throw new AccessDeniedException("Dispositivo ajeno");
        return device;
    }

    @Transactional(readOnly = true)
    public List<Device> findAll() {
        return repository.findAll().stream().filter(this::canRead).toList();
    }

    @Transactional(readOnly = true)
    public List<UUID> visibleIds(UUID deviceId) {
        if (deviceId != null) return List.of(get(deviceId).getId());
        return findAll().stream().map(Device::getId).toList();
    }

    @Transactional
    public Device lockOwned(UUID id) {
        var device = repository.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
        if (!canRead(device)) throw new AccessDeniedException("Dispositivo ajeno");
        return device;
    }

    @Transactional
    public Device assignSector(UUID deviceId, UUID sectorId) {
        farms.requireManager();
        Device device = lockOwned(deviceId);
        farms.requireSector(sectorId);
        device.assignSector(sectorId);
        return repository.save(device);
    }

    @Transactional
    public void markSeen(UUID deviceId) {
        Device device = lockOwned(deviceId);
        device.setLastSeen(Instant.now());
        if (device.getStatus() != DeviceStatus.MAINTENANCE) device.setStatus(DeviceStatus.ONLINE);
    }
}
