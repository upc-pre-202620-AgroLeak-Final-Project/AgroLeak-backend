package pe.edu.upc.agroleak.demo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pe.edu.upc.agroleak.monitoring.application.ReadingService;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;
import java.util.Map;
import java.util.UUID;
@Service
@Profile("!prod")
@ConditionalOnProperty(prefix="agroleak",name="demo-data-enabled",havingValue="true")
public class DemoScenarioService {
    private final ReadingService readings;
    public DemoScenarioService(ReadingService readings) { this.readings=readings; }
    public void normal(UUID deviceId) { snapshot(deviceId,25,24.2,2.2,48); }
    public void leak(UUID deviceId) { snapshot(deviceId,25,17.5,2.0,41); }
    public void obstruction(UUID deviceId) { snapshot(deviceId,20,3.5,3.6,41); }
    private void snapshot(UUID deviceId,double in,double out,double pressure,double soil) {
        readings.recordSnapshot(deviceId,Map.of(SensorType.FLOW_IN,in,SensorType.FLOW_OUT,out,SensorType.PRESSURE,pressure,SensorType.SOIL_MOISTURE,soil),null);
    }
}
