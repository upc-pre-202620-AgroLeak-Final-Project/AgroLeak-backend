package pe.edu.upc.agroleak.valve.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.valve.application.ValveService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/valves")
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

    @GetMapping("/{deviceId}/latest")
    public ValveCommandResponse latest(@PathVariable UUID deviceId) {
        return service.latest(deviceId).map(ValveCommandResponse::from).orElse(null);
    }
}
