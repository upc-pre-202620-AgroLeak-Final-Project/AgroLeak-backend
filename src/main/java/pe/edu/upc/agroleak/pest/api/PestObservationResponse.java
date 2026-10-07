package pe.edu.upc.agroleak.pest.api;

import pe.edu.upc.agroleak.pest.domain.PestObservation;

import java.time.Instant;
import java.util.UUID;

public record PestObservationResponse(
        UUID id, UUID deviceId, int pestCount, double confidence, String imageUrl, Instant recordedAt
) {
    public static PestObservationResponse from(PestObservation observation) {
        return new PestObservationResponse(observation.getId(), observation.getDevice().getId(), observation.getPestCount(),
                observation.getConfidence(), observation.getImageUrl(), observation.getRecordedAt());
    }
}
