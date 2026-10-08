package pe.edu.upc.agroleak.monitoring.domain.model;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.*;
class FlowVolumeCalculatorTest {
    final UUID id=UUID.randomUUID(); final Instant start=Instant.parse("2026-01-01T00:00:00Z");
    @Test void integratesRateOverTimeAndOnlyPairedLoss() {
        var samples=List.of(new FlowVolumeCalculator.Sample(id,SensorType.FLOW_IN,start,60),new FlowVolumeCalculator.Sample(id,SensorType.FLOW_OUT,start,50));
        var result=new FlowVolumeCalculator().calculate(samples,start,start.plusSeconds(600),300);
        assertThat(result.liters()).isEqualTo(300);assertThat(result.lossLiters()).isEqualTo(50);
        assertThat(result.coveredDeviceSeconds()).isEqualTo(300);assertThat(result.pairedDeviceSeconds()).isEqualTo(300);
    }
    @Test void missingOutflowDoesNotImplyOneHundredPercentLossAndBoundaryIsClipped() {
        var samples=List.of(new FlowVolumeCalculator.Sample(id,SensorType.FLOW_IN,start.minusSeconds(60),60));
        var result=new FlowVolumeCalculator().calculate(samples,start,start.plusSeconds(300),300);
        assertThat(result.liters()).isEqualTo(240);assertThat(result.lossLiters()).isZero();
    }
}
