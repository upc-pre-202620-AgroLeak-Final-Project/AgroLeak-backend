package pe.edu.upc.agroleak.devices.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDeviceRequest(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "La ubicación es obligatoria") String location
) {}
