package pe.edu.upc.agroleak.irrigation.presentation.rest.dto;

import jakarta.validation.constraints.NotNull;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveAction;

public record CreateValveCommandRequest(@NotNull(message = "action es obligatorio") ValveAction action) {}
