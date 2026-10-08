package pe.edu.upc.agroleak.farm.presentation.rest.dto;

import pe.edu.upc.agroleak.farm.domain.model.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FieldRequest(@NotBlank @Size(max = 100) String name,
                           @NotNull @Positive @Digits(integer = 10, fraction = 4) BigDecimal areaHectares,
                           @Size(max = 1000) String description) {
}
