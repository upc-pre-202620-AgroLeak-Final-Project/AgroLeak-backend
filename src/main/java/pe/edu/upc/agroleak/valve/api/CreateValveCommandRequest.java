package pe.edu.upc.agroleak.valve.api;

import jakarta.validation.constraints.NotNull;
import pe.edu.upc.agroleak.valve.domain.ValveAction;

public record CreateValveCommandRequest(@NotNull(message = "action es obligatorio") ValveAction action) {}
