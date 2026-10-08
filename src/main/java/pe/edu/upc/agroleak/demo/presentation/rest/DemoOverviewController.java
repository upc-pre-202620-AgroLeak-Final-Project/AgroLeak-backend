package pe.edu.upc.agroleak.demo.presentation.rest;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.presentation.rest.dto.DeviceResponse;
import pe.edu.upc.agroleak.farm.application.FarmService;
import pe.edu.upc.agroleak.farm.presentation.rest.dto.FarmResponse;
import java.util.*;
@RestController
@Profile("!prod")
@ConditionalOnProperty(prefix="agroleak",name="demo-data-enabled",havingValue="true")
@Tag(name="Demo")
@RequestMapping("/api/v1/demo")
public class DemoOverviewController {
    private final DeviceService devices;private final FarmService farms;
    public DemoOverviewController(DeviceService devices,FarmService farms) { this.devices=devices;this.farms=farms; }
    public record Overview(List<FarmResponse> farms,List<DeviceResponse> devices,UUID scenarioDeviceId) {}
    @GetMapping public Overview overview() {
        var visible=devices.findAll();
        return new Overview(farms.listFarms().stream().map(FarmResponse::from).toList(),visible.stream().map(DeviceResponse::from).toList(),
                visible.stream().filter(d -> d.getName().equals("Demo Gateway")).map(d -> d.getId()).findFirst().orElse(null));
    }
}
