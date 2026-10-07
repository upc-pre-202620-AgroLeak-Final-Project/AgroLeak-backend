package pe.edu.upc.agroleak.monitoring.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.upc.agroleak.alert.domain.AlertType;
import pe.edu.upc.agroleak.monitoring.config.DetectionProperties;
import pe.edu.upc.agroleak.monitoring.domain.AlertCandidate;
import pe.edu.upc.agroleak.monitoring.domain.SensorSnapshot;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DetectionEngineTest {
    private DetectionEngine engine;

    @BeforeEach
    void setUp() {
        DetectionProperties properties = new DetectionProperties();
        properties.setLeakThresholdPercent(20.0);
        properties.setMinFlowForAnalysis(3.0);
        properties.setPressureMinBar(1.0);
        properties.setPressureMaxBar(4.0);
        properties.setObstructionFlowOutThreshold(5.0);
        properties.setObstructionPressureThreshold(3.2);
        engine = new DetectionEngine(properties);
    }

    @Test
    void shouldDetectLeakWhenFlowLossIsOverThreshold() {
        List<AlertCandidate> result = engine.evaluate(new SensorSnapshot(25.0, 17.5, 2.0));
        assertThat(result).extracting(AlertCandidate::type).contains(AlertType.LEAK);
    }

    @Test
    void shouldNotRaiseAlertForNormalIrrigation() {
        List<AlertCandidate> result = engine.evaluate(new SensorSnapshot(25.0, 24.0, 2.2));
        assertThat(result).isEmpty();
    }

    @Test
    void shouldDetectObstructionWithLowOutputAndHighPressure() {
        List<AlertCandidate> result = engine.evaluate(new SensorSnapshot(20.0, 3.5, 3.6));
        assertThat(result).extracting(AlertCandidate::type).contains(AlertType.OBSTRUCTION);
    }

    @Test
    void shouldDetectPressureOutsideConfiguredRange() {
        List<AlertCandidate> result = engine.evaluate(new SensorSnapshot(null, null, 4.5));
        assertThat(result).extracting(AlertCandidate::type).contains(AlertType.PRESSURE_OUT_OF_RANGE);
    }
}
