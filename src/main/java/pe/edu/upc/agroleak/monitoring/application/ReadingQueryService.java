package pe.edu.upc.agroleak.monitoring.application;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import pe.edu.upc.agroleak.common.domain.TimeRange;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.monitoring.domain.model.*;
import pe.edu.upc.agroleak.monitoring.infrastructure.SensorReadingRepository;
import java.time.Instant;
import java.util.*;

@Service
@Transactional(readOnly=true)
public class ReadingQueryService {
    private final SensorReadingRepository readings;
    private final DeviceService devices;
    public ReadingQueryService(SensorReadingRepository readings, DeviceService devices) { this.readings=readings; this.devices=devices; }
    private Specification<SensorReading> filter(List<UUID> ids, TimeRange range, SensorType type) {
        return (root,q,cb) -> {
            var p=cb.and(root.get("device").get("id").in(ids), cb.greaterThanOrEqualTo(root.get("recordedAt"),range.from()), cb.lessThan(root.get("recordedAt"),range.to()));
            return type == null ? p : cb.and(p, cb.equal(root.get("sensorType"),type));
        };
    }
    public List<SensorReading> history(UUID deviceId, Instant from, Instant to, SensorType type, int limit) {
        var range=TimeRange.of(from,to);
        TimeRange.limit(limit);
        var ids=devices.visibleIds(deviceId);
        if(ids.isEmpty()) return List.of();
        return readings.findAll(filter(ids,range,type),PageRequest.of(0,limit,Sort.by(Sort.Direction.DESC,"recordedAt","id"))).getContent();
    }
    public Optional<SensorReading> latest(UUID deviceId) {
        devices.get(deviceId);
        return readings.findTopByDeviceIdOrderByRecordedAtDesc(deviceId);
    }
    public Map<SensorType,ReadingStatistics> summary(UUID deviceId, Instant from, Instant to) {
        devices.get(deviceId);
        var range=TimeRange.of(from,to);
        Map<SensorType,ReadingStatistics> result=new EnumMap<>(SensorType.class);
        for(var type:SensorType.values()) result.put(type,new ReadingStatistics(null,null,null,0));
        readings.summarize(deviceId,range.from(),range.to()).forEach(a -> result.put(a.getSensorType(),new ReadingStatistics(a.getAverage(),a.getMin(),a.getMax(),a.getSamples())));
        return result;
    }
    /** Complete bounded series for analytics; never silently truncates totals. */
    public List<SensorReading> series(UUID deviceId, TimeRange range) {
        var ids=devices.visibleIds(deviceId);
        if(ids.isEmpty()) return List.of();
        var page=readings.findAll(filter(ids,range,null),PageRequest.of(0,100001,Sort.by("recordedAt","id")));
        if(page.getTotalElements()>100000) throw new IllegalArgumentException("Demasiadas lecturas; reduzca el rango o filtre deviceId");
        var result=new ArrayList<>(page.getContent());
        for(var id:ids) for(var type:List.of(SensorType.FLOW_IN,SensorType.FLOW_OUT))
            readings.findTopByDeviceIdAndSensorTypeAndRecordedAtLessThanOrderByRecordedAtDesc(id,type,range.from()).ifPresent(result::add);
        return result;
    }
}
