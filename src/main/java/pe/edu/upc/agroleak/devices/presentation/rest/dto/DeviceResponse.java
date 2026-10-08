package pe.edu.upc.agroleak.devices.presentation.rest.dto;

import pe.edu.upc.agroleak.devices.domain.model.*;

import java.time.*;
import java.util.UUID;

public record DeviceResponse(UUID id, String name, String location, DeviceStatus status, Instant lastSeen,
                             DeviceType deviceType, Integer batteryLevel, String firmwareVersion,
                             LocalDate installationDate, UUID sectorId) {
    public static DeviceResponse from(Device d) {
        return new DeviceResponse(d.getId(), d.getName(), d.getLocation(), d.getStatus(), d.getLastSeen(),
                d.getDeviceType(), d.getBatteryLevel(), d.getFirmwareVersion(), d.getInstallationDate(), d.getSectorId());
    }
}
