package pe.edu.upc.agroleak.analytics.presentation.rest;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.agroleak.analytics.application.AnalyticsQueryService;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/analytics")
@Tag(name="Analytics")
public class AnalyticsController {
    private final AnalyticsQueryService service;
    public AnalyticsController(AnalyticsQueryService service) { this.service=service; }
    @GetMapping("/dashboard") public AnalyticsQueryService.Dashboard dashboard() { return service.dashboard(); }
    @GetMapping("/charts") public AnalyticsQueryService.Charts charts(@RequestParam(required=false) UUID deviceId) { return service.charts(deviceId); }
}
