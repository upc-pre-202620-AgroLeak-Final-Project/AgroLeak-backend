package pe.edu.upc.agroleak.devices.presentation.rest;

import pe.edu.upc.agroleak.devices.presentation.rest.dto.CreateDeviceRequest;
import pe.edu.upc.agroleak.devices.presentation.rest.dto.DeviceResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.devices.application.DeviceService;

import java.util.List;
import java.util.UUID;

@io.swagger.v3.oas.annotations.tags.Tag(name="Devices")
@RestController
@RequestMapping("/api/v1/devices")
public class DeviceController {
    private final DeviceService service;

    public DeviceController(DeviceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DeviceResponse create(@Valid @RequestBody CreateDeviceRequest request) {
        return DeviceResponse.from(service.create(request.name(), request.location(), request.deviceType(), request.status(), request.batteryLevel(), request.firmwareVersion(), request.installationDate(), request.sectorId()));
    }

    @PatchMapping("/{id}/sector")
    public DeviceResponse assignSector(@PathVariable UUID id, @Valid @RequestBody pe.edu.upc.agroleak.devices.presentation.rest.dto.AssignSectorRequest request) {
        return DeviceResponse.from(service.assignSector(id, request.sectorId()));
    }

    @GetMapping
    public List<DeviceResponse> findAll() {
        return service.findAll().stream().map(DeviceResponse::from).toList();
    }

    @GetMapping("/{id}")
    public DeviceResponse get(@PathVariable UUID id) {
        return DeviceResponse.from(service.get(id));
    }
}
