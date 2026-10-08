package pe.edu.upc.agroleak.alerts.presentation.rest;

import pe.edu.upc.agroleak.alerts.presentation.rest.dto.AlertResponse;

import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.alerts.application.AlertService;
import pe.edu.upc.agroleak.alerts.domain.model.AlertStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {
    private final AlertService service;

    public AlertController(AlertService service) {
        this.service = service;
    }

    @GetMapping
    public List<AlertResponse> find(@RequestParam UUID deviceId, @RequestParam(required = false) AlertStatus status) {
        return service.findByDevice(deviceId, status).stream().map(AlertResponse::from).toList();
    }

    @PatchMapping("/{id}/resolve")
    public AlertResponse resolve(@PathVariable UUID id) {
        return AlertResponse.from(service.resolve(id));
    }
}
