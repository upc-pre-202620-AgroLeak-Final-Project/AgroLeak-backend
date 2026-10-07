package pe.edu.upc.agroleak.device.api;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "La ubicación es obligatoria") String location
) {}
