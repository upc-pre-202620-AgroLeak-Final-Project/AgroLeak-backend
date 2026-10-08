package pe.edu.upc.agroleak.analytics.presentation.rest;

import pe.edu.upc.agroleak.analytics.presentation.rest.dto.DashboardResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upc.agroleak.analytics.application.DashboardService;

import java.util.UUID;

@io.swagger.v3.oas.annotations.tags.Tag(name="Analytics")
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/{deviceId}")
    public DashboardResponse get(@PathVariable UUID deviceId) {
        return service.get(deviceId);
    }
}
