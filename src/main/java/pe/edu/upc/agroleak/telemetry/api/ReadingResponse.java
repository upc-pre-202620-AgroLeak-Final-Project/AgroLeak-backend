package pe.edu.upc.agroleak.telemetry.api;

import pe.edu.upc.agroleak.telemetry.domain.SensorReading;
import pe.edu.upc.agroleak.telemetry.domain.SensorType;

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
