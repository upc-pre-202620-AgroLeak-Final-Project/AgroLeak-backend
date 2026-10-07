package pe.edu.upc.agroleak.device.api;

import pe.edu.upc.agroleak.device.domain.Device;
import pe.edu.upc.agroleak.device.domain.DeviceStatus;

import java.time.Instant;
import java.util.UUID;

public record DeviceResponse(UUID id, String name, String location, DeviceStatus status, Instant lastSeen) {
    public static DeviceResponse from(Device device) {
        return new DeviceResponse(device.getId(), device.getName(), device.getLocation(), device.getStatus(), device.getLastSeen());
    }
}
