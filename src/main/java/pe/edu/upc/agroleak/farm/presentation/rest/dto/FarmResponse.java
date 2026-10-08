package pe.edu.upc.agroleak.farm.presentation.rest.dto;

import pe.edu.upc.agroleak.farm.domain.model.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

public record FarmResponse(UUID id, String name, String location, BigDecimal areaHectares, UUID ownerId,
                           Instant createdAt, Instant updatedAt) {
    public static FarmResponse from(Farm e) {
        return new FarmResponse(e.getId(), e.getName(), e.getLocation(), e.getAreaHectares(), e.getOwnerId(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
