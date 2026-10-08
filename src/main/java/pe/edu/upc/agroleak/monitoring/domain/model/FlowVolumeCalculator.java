package pe.edu.upc.agroleak.monitoring.domain.model;
import java.time.*;
import java.util.*;

/** Zero-order hold, L/min -> liters; expired/missing samples never imply continuous consumption. */
public class FlowVolumeCalculator {
    public record Sample(UUID deviceId, SensorType type, Instant at, double value) {}
    public record Volume(double liters, double lossLiters, double coveredDeviceSeconds, double pairedDeviceSeconds) {}
    public Volume calculate(List<Sample> samples,Instant from,Instant to,long maxHoldSeconds) {
        if(maxHoldSeconds<1) throw new IllegalArgumentException("maxHoldSeconds debe ser positivo");
        Map<UUID,List<Sample>> groups=new HashMap<>();
        samples.stream().filter(s -> s.type()==SensorType.FLOW_IN || s.type()==SensorType.FLOW_OUT)
                .forEach(s -> groups.computeIfAbsent(s.deviceId(),k -> new ArrayList<>()).add(s));
        double liters=0,loss=0,covered=0,paired=0;
        for(var device:groups.values()) {
            NavigableMap<Instant,Double> in=new TreeMap<>(),out=new TreeMap<>();
            TreeSet<Instant> boundaries=new TreeSet<>(List.of(from,to));
            for(var s:device) {
                (s.type()==SensorType.FLOW_IN ? in : out).put(s.at(),s.value());
                if(s.at().isAfter(from) && s.at().isBefore(to)) boundaries.add(s.at());
                Instant expiry=s.at().plusSeconds(maxHoldSeconds);
                if(expiry.isAfter(from) && expiry.isBefore(to)) boundaries.add(expiry);
            }
            var points=new ArrayList<>(boundaries);
            for(int i=0;i<points.size()-1;i++) {
                Instant start=points.get(i);
                double seconds=Duration.between(start,points.get(i+1)).toNanos()/1_000_000_000.0;
                var a=in.floorEntry(start);var b=out.floorEntry(start);
                boolean hasIn=a!=null && a.getKey().plusSeconds(maxHoldSeconds).isAfter(start);
                boolean hasOut=b!=null && b.getKey().plusSeconds(maxHoldSeconds).isAfter(start);
                if(hasIn) { liters+=a.getValue()*seconds/60;covered+=seconds; }
                if(hasIn && hasOut) { loss+=Math.max(0,a.getValue()-b.getValue())*seconds/60;paired+=seconds; }
            }
        }
        return new Volume(liters,loss,covered,paired);
    }
}
