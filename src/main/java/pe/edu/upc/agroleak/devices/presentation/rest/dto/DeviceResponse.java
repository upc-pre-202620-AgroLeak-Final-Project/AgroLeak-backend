package pe.edu.upc.agroleak.devices.presentation.rest.dto;

import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.devices.domain.model.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(UUID id, String name, String location, DeviceStatus status, Instant lastSeen) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(device.getId(), device.getName(), device.getLocation(), device.getStatus(), device.getLastSeen());
    }
}
