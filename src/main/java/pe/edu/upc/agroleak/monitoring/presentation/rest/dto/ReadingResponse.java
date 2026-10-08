package pe.edu.upc.agroleak.monitoring.presentation.rest.dto;

import pe.edu.upc.agroleak.monitoring.domain.model.SensorReading;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;

import java.time.Instant;
import java.util.UUID;

public record ReadingResponse(
        UUID id,
        UUID deviceId,
        SensorType sensorType,
        double value,
        String unit,
        Instant recordedAt
) {
    public static ReadingResponse from(SensorReading reading) {
        return new ReadingResponse(
                reading.getId(),
                reading.getDevice().getId(),
                reading.getSensorType(),
                reading.getValue(),
                reading.getUnit(),
                reading.getRecordedAt());
    }
}
