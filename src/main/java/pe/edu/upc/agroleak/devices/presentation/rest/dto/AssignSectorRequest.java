package pe.edu.upc.agroleak.devices.presentation.rest.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignSectorRequest(@NotNull UUID sectorId) {
}
