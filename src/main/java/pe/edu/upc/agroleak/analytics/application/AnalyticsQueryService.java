package pe.edu.upc.agroleak.analytics.application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import pe.edu.upc.agroleak.common.domain.TimeRange;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.DeviceStatus;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.alerts.domain.model.*;
import pe.edu.upc.agroleak.pests.application.PestObservationService;
import pe.edu.upc.agroleak.monitoring.application.ReadingQueryService;
import pe.edu.upc.agroleak.monitoring.domain.model.*;
import java.time.*;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class AnalyticsQueryService {
    private final ReadingQueryService readings;
    private final DeviceService devices;
    private final AlertService alerts;
    private final PestObservationService pests;
    private final long maxHoldSeconds;
    private final FlowVolumeCalculator calculator=new FlowVolumeCalculator();
    public AnalyticsQueryService(ReadingQueryService readings,DeviceService devices,AlertService alerts,PestObservationService pests,
            @Value("${agroleak.analytics.max-hold-seconds:300}") long maxHoldSeconds) {
        if(maxHoldSeconds<1) throw new IllegalArgumentException("max-hold-seconds debe ser positivo");
        this.readings=readings;this.devices=devices;this.alerts=alerts;this.pests=pests;this.maxHoldSeconds=maxHoldSeconds;
    }
    public record Dashboard(double waterUsageToday,double estimatedWaterLoss,long activeAlerts,long criticalAlerts,
            long onlineDevices,long offlineDevices,long maintenanceDevices,long pestsDetectedToday,
            String volumeUnit,String timezone,Instant from,Instant to,double coveredDeviceSeconds,double pairedDeviceSeconds) {}
    public record Point(Instant from,Instant to,double waterUsage,double estimatedWaterLoss,Double flowIn,Double flowOut,Double pressure,Double soilMoisture) {}
    public record Charts(List<Point> hourly,Map<AlertType,Long> alertsByType,Map<AlertSeverity,Long> alertsBySeverity,
            String volumeUnit,String timezone,long maxHoldSeconds) {}
    private List<FlowVolumeCalculator.Sample> samples(List<SensorReading> data) {
        return data.stream().map(r -> new FlowVolumeCalculator.Sample(r.getDevice().getId(),r.getSensorType(),r.getRecordedAt(),r.getValue())).toList();
    }
    public Dashboard dashboard() {
        Instant now=Instant.now();Instant start=now.atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        var data=readings.series(null,new TimeRange(start,now));
        var volume=calculator.calculate(samples(data),start,now,maxHoldSeconds);
        var counts=alerts.overview(null);
        var visible=devices.findAll();
        return new Dashboard(volume.liters(),volume.lossLiters(),counts.activeAlerts(),counts.criticalAlerts(),
                visible.stream().filter(d -> d.getStatus()==DeviceStatus.ONLINE).count(),
                visible.stream().filter(d -> d.getStatus()==DeviceStatus.OFFLINE).count(),
                visible.stream().filter(d -> d.getStatus()==DeviceStatus.MAINTENANCE).count(),pests.total(start,now),
                "L","UTC",start,now,volume.coveredDeviceSeconds(),volume.pairedDeviceSeconds());
    }
    public Charts charts(UUID deviceId) {
        Instant end=Instant.now(),start=end.minus(Duration.ofHours(24));
        var data=readings.series(deviceId,new TimeRange(start,end));
        var samples=samples(data);
        List<Point> result=new ArrayList<>();
        for(int i=0;i<24;i++) {
            Instant a=start.plus(Duration.ofHours(i)),b=a.plus(Duration.ofHours(1));
            var v=calculator.calculate(samples,a,b,maxHoldSeconds);
            Map<SensorType,DoubleSummaryStatistics> stats=new EnumMap<>(SensorType.class);
            data.stream().filter(r -> !r.getRecordedAt().isBefore(a) && r.getRecordedAt().isBefore(b))
                    .forEach(r -> stats.computeIfAbsent(r.getSensorType(),k -> new DoubleSummaryStatistics()).accept(r.getValue()));
            result.add(new Point(a,b,v.liters(),v.lossLiters(),avg(stats,SensorType.FLOW_IN),avg(stats,SensorType.FLOW_OUT),avg(stats,SensorType.PRESSURE),avg(stats,SensorType.SOIL_MOISTURE)));
        }
        var counts=alerts.overview(deviceId);
        return new Charts(result,counts.byType(),counts.bySeverity(),"L","UTC",maxHoldSeconds);
    }
    private Double avg(Map<SensorType,DoubleSummaryStatistics> stats,SensorType type) {
        return stats.containsKey(type) ? stats.get(type).getAverage() : null;
    }
}
