package pe.edu.upc.agroleak.dashboard.api;

import pe.edu.upc.agroleak.device.domain.DeviceStatus;
import pe.edu.upc.agroleak.valve.domain.ValveAction;
import pe.edu.upc.agroleak.valve.domain.ValveCommandStatus;

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
