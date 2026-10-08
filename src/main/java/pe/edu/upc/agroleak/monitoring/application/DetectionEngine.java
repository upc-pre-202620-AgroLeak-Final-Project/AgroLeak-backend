package pe.edu.upc.agroleak.monitoring.application;

import org.springframework.stereotype.Component;
import pe.edu.upc.agroleak.alerts.domain.model.AlertSeverity;
import pe.edu.upc.agroleak.alerts.domain.model.AlertType;
import pe.edu.upc.agroleak.monitoring.infrastructure.config.DetectionProperties;
import pe.edu.upc.agroleak.monitoring.domain.model.AlertCandidate;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorSnapshot;

import java.util.ArrayList;
import java.util.List;

@Component
public class DetectionEngine {
    private final DetectionProperties properties;

    public DetectionEngine(DetectionProperties properties) {
        this.properties = properties;
    }

    public List<AlertCandidate> evaluate(SensorSnapshot snapshot) {
        List<AlertCandidate> alerts = new ArrayList<>();

        if (snapshot.pressure() != null &&
                (snapshot.pressure() < properties.getPressureMinBar() || snapshot.pressure() > properties.getPressureMaxBar())) {
            alerts.add(new AlertCandidate(
                    snapshot.pressure() < properties.getPressureMinBar() ? AlertType.LOW_PRESSURE : AlertType.HIGH_PRESSURE,
                    AlertSeverity.MEDIUM,
                    "Presión fuera del rango esperado: %.2f bar".formatted(snapshot.pressure())));
        }

        if (snapshot.flowIn() != null && snapshot.flowOut() != null && snapshot.flowIn() >= properties.getMinFlowForAnalysis()) {
            double lossPercent = Math.max(0, ((snapshot.flowIn() - snapshot.flowOut()) / snapshot.flowIn()) * 100.0);
            if (lossPercent >= properties.getLeakThresholdPercent()) {
                alerts.add(new AlertCandidate(
                        AlertType.LEAK,
                        AlertSeverity.HIGH,
                        "Posible fuga: diferencia de caudal de %.1f%% (entrada %.2f / salida %.2f L/min)"
                                .formatted(lossPercent, snapshot.flowIn(), snapshot.flowOut())));
            }
        }

        if (snapshot.flowOut() != null && snapshot.pressure() != null
                && snapshot.flowOut() <= properties.getObstructionFlowOutThreshold()
                && snapshot.pressure() >= properties.getObstructionPressureThreshold()) {
            alerts.add(new AlertCandidate(
                    AlertType.OBSTRUCTION,
                    AlertSeverity.HIGH,
                    "Posible obstrucción: caudal de salida bajo con presión elevada"));
        }

        return alerts;
    }
}
