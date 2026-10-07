package pe.edu.upc.agroleak.pest.api;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public record CreatePestObservationRequest(
        @NotNull(message = "deviceId es obligatorio") UUID deviceId,
        @PositiveOrZero(message = "pestCount no puede ser negativo") int pestCount,
        @DecimalMin(value = "0.0", message = "confidence debe ser >= 0")
        @DecimalMax(value = "1.0", message = "confidence debe ser <= 1") double confidence,
        String imageUrl,
        Instant recordedAt
) {}
