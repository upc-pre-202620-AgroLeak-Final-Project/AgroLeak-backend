package pe.edu.upc.agroleak.monitoring.presentation.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;

import java.time.Instant;
import java.util.UUID;

public record CreateReadingRequest(
        @NotNull(message = "deviceId es obligatorio") UUID deviceId,
        @NotNull(message = "sensorType es obligatorio") SensorType sensorType,
        @PositiveOrZero(message = "value no puede ser negativo") @NotNull Double value,
        @NotBlank(message = "unit es obligatorio") @jakarta.validation.constraints.Size(max=20) String unit,
        @jakarta.validation.constraints.PastOrPresent Instant recordedAt
) {}
