package pe.edu.upc.agroleak.devices.presentation.rest.dto;

import jakarta.validation.constraints.*;
import pe.edu.upc.agroleak.devices.domain.model.*;

import java.time.LocalDate;
import java.util.UUID;

public record CreateDeviceRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 120) String location,
        DeviceType deviceType, DeviceStatus status,
        @Min(0) @Max(100) Integer batteryLevel,
        @Size(max = 100) String firmwareVersion,
        @PastOrPresent LocalDate installationDate, UUID sectorId) {
}
