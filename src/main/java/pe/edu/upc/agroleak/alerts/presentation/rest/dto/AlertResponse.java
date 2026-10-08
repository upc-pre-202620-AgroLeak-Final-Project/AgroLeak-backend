package pe.edu.upc.agroleak.alerts.presentation.rest.dto;

import pe.edu.upc.agroleak.alerts.domain.model.*;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
        UUID id, UUID deviceId, AlertType type, AlertSeverity severity, String message,
        AlertStatus status, Instant createdAt, Instant resolvedAt, Instant acknowledgedAt, UUID acknowledgedBy, UUID resolvedBy, UUID sectorId
) {
    public static AlertResponse from(Alert alert) {
        return new AlertResponse(alert.getId(), alert.getDevice()==null ? null : alert.getDevice().getId(), alert.getType(), alert.getSeverity(),
                alert.getMessage(), alert.getStatus(), alert.getCreatedAt(), alert.getResolvedAt(), alert.getAcknowledgedAt(), alert.getAcknowledgedBy(), alert.getResolvedBy(), alert.getSectorId());
    }
}
