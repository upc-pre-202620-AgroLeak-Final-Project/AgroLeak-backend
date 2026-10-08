package pe.edu.upc.agroleak.pests.presentation.rest.dto;

import pe.edu.upc.agroleak.pests.domain.model.PestObservation;

import java.time.Instant;
import java.util.UUID;

public record PestObservationResponse(
        UUID id, UUID deviceId, int pestCount, double confidence, String imageUrl, Instant recordedAt, UUID sectorId, String pestType, int count, Instant detectedAt
) {
    public static PestObservationResponse from(PestObservation observation) {
        return new PestObservationResponse(observation.getId(), observation.getDevice() == null ? null : observation.getDevice().getId(), observation.getPestCount(),
                observation.getConfidence(), observation.getImageUrl(), observation.getRecordedAt(), observation.getSectorId(), observation.getPestType(), observation.getPestCount(), observation.getRecordedAt());
    }
}
