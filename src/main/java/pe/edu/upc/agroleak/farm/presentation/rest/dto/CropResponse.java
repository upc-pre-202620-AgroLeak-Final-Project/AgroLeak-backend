package pe.edu.upc.agroleak.farm.presentation.rest.dto;

import pe.edu.upc.agroleak.farm.domain.model.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.*;

public record CropResponse(UUID id, UUID sectorId, String name, String variety, LocalDate plantedAt,
                           CropStatus status) {
    public static CropResponse from(Crop e) {
        return new CropResponse(e.getId(), e.getSectorId(), e.getName(), e.getVariety(), e.getPlantedAt(), e.getStatus());
    }
}
