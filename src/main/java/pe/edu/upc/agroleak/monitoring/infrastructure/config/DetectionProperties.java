package pe.edu.upc.agroleak.monitoring.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agroleak.detection")
public class DetectionProperties {
    private double leakThresholdPercent = 20.0;
    private double minFlowForAnalysis = 3.0;
    private double pressureMinBar = 1.0;
    private double pressureMaxBar = 4.0;
    private double obstructionFlowOutThreshold = 5.0;
    private double obstructionPressureThreshold = 3.2;
    private long pairWindowSeconds = 120;

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
