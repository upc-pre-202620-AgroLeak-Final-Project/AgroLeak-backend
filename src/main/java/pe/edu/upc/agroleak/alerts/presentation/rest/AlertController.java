package pe.edu.upc.agroleak.alerts.presentation.rest;

import pe.edu.upc.agroleak.alerts.presentation.rest.dto.AlertResponse;

import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.alerts.domain.model.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
@io.swagger.v3.oas.annotations.tags.Tag(name="Alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlertResponse> find(@RequestParam(required=false) UUID deviceId, @RequestParam(required = false) AlertStatus status,
            @RequestParam(required=false) AlertSeverity severity,@RequestParam(required=false) AlertType type,@RequestParam(defaultValue="100") int limit) {
        return service.search(deviceId, status, severity, type, limit).stream().map(AlertResponse::from).toList();
    }

    @PatchMapping("/{id}/acknowledge")
    public AlertResponse acknowledge(@PathVariable UUID id) { return AlertResponse.from(service.acknowledge(id)); }

    @PatchMapping("/{id}/resolve")
    public AlertResponse resolve(@PathVariable UUID id) {
        return AlertResponse.from(service.resolve(id));
    }
}
