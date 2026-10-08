package pe.edu.upc.agroleak.monitoring.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@org.springframework.validation.annotation.Validated
@ConfigurationProperties(prefix = "agroleak.detection")
public class DetectionProperties {
    @jakarta.validation.constraints.Positive
    private double leakThresholdPercent = 20.0;
    @jakarta.validation.constraints.Positive
    private double minFlowForAnalysis = 3.0;
    @jakarta.validation.constraints.PositiveOrZero
    private double pressureMinBar = 1.0;
    @jakarta.validation.constraints.Positive
    private double pressureMaxBar = 4.0;
    @jakarta.validation.constraints.PositiveOrZero
    private double obstructionFlowOutThreshold = 5.0;
    @jakarta.validation.constraints.Positive
    private double obstructionPressureThreshold = 3.2;
    @jakarta.validation.constraints.Positive
    private long pairWindowSeconds = 120;

    @jakarta.validation.constraints.AssertTrue(message="pressure-min-bar debe ser menor a pressure-max-bar y leak-threshold-percent <= 100")
    public boolean isThresholdRangeValid() { return pressureMinBar < pressureMaxBar && leakThresholdPercent <= 100; }

    public double getLeakThresholdPercent() { return leakThresholdPercent; }
    public void setLeakThresholdPercent(double value) { this.leakThresholdPercent = value; }
    public double getMinFlowForAnalysis() { return minFlowForAnalysis; }
    public void setMinFlowForAnalysis(double value) { this.minFlowForAnalysis = value; }
    public double getPressureMinBar() { return pressureMinBar; }
    public void setPressureMinBar(double value) { this.pressureMinBar = value; }
    public double getPressureMaxBar() { return pressureMaxBar; }
    public void setPressureMaxBar(double value) { this.pressureMaxBar = value; }
    public double getObstructionFlowOutThreshold() { return obstructionFlowOutThreshold; }
    public void setObstructionFlowOutThreshold(double value) { this.obstructionFlowOutThreshold = value; }
    public double getObstructionPressureThreshold() { return obstructionPressureThreshold; }
    public void setObstructionPressureThreshold(double value) { this.obstructionPressureThreshold = value; }
    public long getPairWindowSeconds() { return pairWindowSeconds; }
    public void setPairWindowSeconds(long value) { this.pairWindowSeconds = value; }
}
