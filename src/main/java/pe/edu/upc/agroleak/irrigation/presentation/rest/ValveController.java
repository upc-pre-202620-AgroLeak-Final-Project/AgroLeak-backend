package pe.edu.upc.agroleak.irrigation.presentation.rest;

import pe.edu.upc.agroleak.irrigation.presentation.rest.dto.ConfirmValveCommandRequest;
import pe.edu.upc.agroleak.irrigation.presentation.rest.dto.CreateValveCommandRequest;
import pe.edu.upc.agroleak.irrigation.presentation.rest.dto.ValveCommandResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.irrigation.application.ValveService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/valves")
@io.swagger.v3.oas.annotations.tags.Tag(name="Irrigation")
public class ValveController {
    private final ValveService service;

    public ValveController(ValveService service) {
        this.service = service;
    }

    @PostMapping("/{deviceId}/commands")
    @ResponseStatus(HttpStatus.CREATED)
    public ValveCommandResponse request(@PathVariable UUID deviceId, @Valid @RequestBody CreateValveCommandRequest request) {
        return ValveCommandResponse.from(service.request(deviceId, request.action()));
    }

    @PatchMapping("/commands/{commandId}/confirm")
    public ValveCommandResponse confirm(@PathVariable UUID commandId, @Valid @RequestBody ConfirmValveCommandRequest request) {
        return ValveCommandResponse.from(service.confirm(commandId, request.success()));
    }

    public record ModeRequest(@jakarta.validation.constraints.NotNull pe.edu.upc.agroleak.irrigation.domain.model.OperationMode mode) {}
    public record ModeResponse(UUID deviceId,pe.edu.upc.agroleak.irrigation.domain.model.OperationMode mode) {}
    @GetMapping("/{deviceId}/mode")
    public ModeResponse mode(@PathVariable UUID deviceId) { return new ModeResponse(deviceId,service.mode(deviceId)); }
    @PutMapping("/{deviceId}/mode")
    public ModeResponse mode(@PathVariable UUID deviceId,@Valid @RequestBody ModeRequest request) { return new ModeResponse(deviceId,service.changeMode(deviceId,request.mode())); }

    @GetMapping("/{deviceId}/latest")
    public ValveCommandResponse latest(@PathVariable UUID deviceId) {
        return service.latest(deviceId).map(ValveCommandResponse::from).orElse(null);
    }
}
