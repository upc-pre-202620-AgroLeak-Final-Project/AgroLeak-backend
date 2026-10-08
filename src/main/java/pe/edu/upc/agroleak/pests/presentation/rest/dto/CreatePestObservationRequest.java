package pe.edu.upc.agroleak.pests.presentation.rest.dto;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.time.Instant;
import java.util.UUID;
public record CreatePestObservationRequest(
        @JsonAlias("cameraId") UUID deviceId,
        @NotNull @PositiveOrZero @JsonAlias("count") Integer pestCount,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") Double confidence,
        @Size(max=500) @Pattern(regexp="https?://[^\\s]+",message="imageUrl debe ser HTTP(S)") String imageUrl,
        @PastOrPresent @JsonAlias("detectedAt") Instant recordedAt,
        UUID sectorId, @Size(max=100) String pestType) {}
