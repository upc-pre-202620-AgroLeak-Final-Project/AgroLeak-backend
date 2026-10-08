package pe.edu.upc.agroleak.irrigation.presentation.rest.dto;

import jakarta.validation.constraints.NotNull;

public record ConfirmValveCommandRequest(@NotNull(message = "success es obligatorio") Boolean success) {}
