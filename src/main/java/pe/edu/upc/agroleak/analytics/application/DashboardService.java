package pe.edu.upc.agroleak.analytics.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.analytics.presentation.rest.dto.DashboardResponse;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.pests.application.PestObservationService;
import pe.edu.upc.agroleak.pests.domain.model.PestObservation;
import pe.edu.upc.agroleak.monitoring.application.ReadingService;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorReading;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;
import pe.edu.upc.agroleak.irrigation.application.ValveService;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommand;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class DashboardService {
    private final DeviceService deviceService;
    private final ReadingService readingService;
    private final AlertService alertService;
    private final ValveService valveService;
    private final PestObservationService pestService;

    public DashboardService(DeviceService deviceService, ReadingService readingService, AlertService alertService,
                            ValveService valveService, PestObservationService pestService) {
        this.deviceService = deviceService;
        this.readingService = readingService;
        this.alertService = alertService;
        this.valveService = valveService;
        this.pestService = pestService;
    }

    @Transactional(readOnly = true)
    public DashboardResponse get(UUID deviceId) {
        Device device = deviceService.get(deviceId);
        Map<SensorType, SensorReading> readings = readingService.latest(deviceId);
        Double flowIn = value(readings, SensorType.FLOW_IN);
        Double flowOut = value(readings, SensorType.FLOW_OUT);
        Double pressure = value(readings, SensorType.PRESSURE);
        Double soil = value(readings, SensorType.SOIL_MOISTURE);
        Double loss = calculateLoss(flowIn, flowOut);
        ValveCommand valve = valveService.latest(deviceId).orElse(null);
        PestObservation pest = pestService.latest(deviceId).orElse(null);

        return new DashboardResponse(
                device.getId(), device.getName(), device.getLocation(), device.getStatus(), device.getLastSeen(),
                flowIn, flowOut, pressure, soil, loss, alertService.activeCount(deviceId),
                valve == null ? null : valve.getAction(),
                valve == null ? null : valve.getStatus(),
                pest == null ? null : pest.getPestCount(),
                pest == null ? null : pest.getConfidence(),
                Instant.now());
    }

    private Double value(Map<SensorType, SensorReading> readings, SensorType type) {
        SensorReading reading = readings.get(type);
        return reading == null ? null : reading.getValue();
    }

    private Double calculateLoss(Double flowIn, Double flowOut) {
        if (flowIn == null || flowOut == null || flowIn <= 0) return null;
        return Math.max(0, ((flowIn - flowOut) / flowIn) * 100.0);
    }
}
