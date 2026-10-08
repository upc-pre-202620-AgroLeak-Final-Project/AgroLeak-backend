package pe.edu.upc.agroleak.farm.presentation.rest.dto;

import pe.edu.upc.agroleak.farm.domain.model.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CropRequest(@NotBlank @Size(max = 100) String name, @Size(max = 100) String variety,
                          @NotNull @PastOrPresent LocalDate plantedAt, @NotNull CropStatus status) {
}
