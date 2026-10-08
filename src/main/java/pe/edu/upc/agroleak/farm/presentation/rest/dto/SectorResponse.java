package pe.edu.upc.agroleak.farm.presentation.rest.dto;

import pe.edu.upc.agroleak.farm.domain.model.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

public record SectorResponse(UUID id, UUID fieldId, String name, BigDecimal areaHectares, SectorStatus status) {
    public static SectorResponse from(Sector e) {
        return new SectorResponse(e.getId(), e.getFieldId(), e.getName(), e.getAreaHectares(), e.getStatus());
    }
}
