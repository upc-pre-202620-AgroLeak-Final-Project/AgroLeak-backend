package pe.edu.upc.agroleak.analytics.presentation.rest.dto;

import pe.edu.upc.agroleak.devices.domain.model.DeviceStatus;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveAction;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommandStatus;

import java.time.Instant;
import java.util.UUID;

public record DashboardResponse(
        UUID deviceId,
        String deviceName,
        String location,
        DeviceStatus deviceStatus,
        Instant lastSeen,
        Double flowIn,
        Double flowOut,
        Double pressure,
        Double soilMoisture,
        Double estimatedLossPercent,
        long activeAlerts,
        ValveAction lastValveAction,
        ValveCommandStatus lastValveCommandStatus,
        Integer latestPestCount,
        Double latestPestConfidence,
        Instant generatedAt
) {}
