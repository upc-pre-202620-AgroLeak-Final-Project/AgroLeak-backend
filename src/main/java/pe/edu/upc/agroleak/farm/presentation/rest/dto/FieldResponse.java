package pe.edu.upc.agroleak.farm.presentation.rest.dto;

import pe.edu.upc.agroleak.farm.domain.model.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

public record FieldResponse(UUID id, UUID farmId, String name, BigDecimal areaHectares, String description) {
    public static FieldResponse from(Field e) {
        return new FieldResponse(e.getId(), e.getFarmId(), e.getName(), e.getAreaHectares(), e.getDescription());
    }
}
