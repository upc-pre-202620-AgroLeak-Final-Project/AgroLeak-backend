package pe.edu.upc.agroleak.irrigation.presentation.rest.dto;

import pe.edu.upc.agroleak.irrigation.domain.model.*;

import java.time.Instant;
import java.util.UUID;

public record ValveCommandResponse(
        UUID id, UUID deviceId, ValveAction action, ValveCommandStatus status, Instant requestedAt, Instant confirmedAt
) {
    public static ValveCommandResponse from(ValveCommand command) {
        return new ValveCommandResponse(command.getId(), command.getDevice().getId(), command.getAction(),
                command.getStatus(), command.getRequestedAt(), command.getConfirmedAt());
    }
}
