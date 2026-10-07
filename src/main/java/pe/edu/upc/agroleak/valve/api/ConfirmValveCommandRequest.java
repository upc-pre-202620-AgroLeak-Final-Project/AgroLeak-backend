package pe.edu.upc.agroleak.valve.api;

import jakarta.validation.constraints.NotNull;

public record ConfirmValveCommandRequest(@NotNull(message = "success es obligatorio") Boolean success) {}
