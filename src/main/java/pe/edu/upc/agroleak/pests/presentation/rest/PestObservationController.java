package pe.edu.upc.agroleak.pests.presentation.rest;

import pe.edu.upc.agroleak.pests.presentation.rest.dto.CreatePestObservationRequest;
import pe.edu.upc.agroleak.pests.presentation.rest.dto.PestObservationResponse;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upc.agroleak.pests.application.PestObservationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/pest-observations","/api/v1/pests/observations"})
@io.swagger.v3.oas.annotations.tags.Tag(name="Pests")
public class PestObservationController {
    private final PestObservationService service;

    public PestObservationController(PestObservationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PestObservationResponse create(@Valid @RequestBody CreatePestObservationRequest request) {
        return PestObservationResponse.from(service.create(request.deviceId(), request.pestCount(), request.confidence(), request.imageUrl(), request.recordedAt(), request.sectorId(), request.pestType()));
    }

    @GetMapping
    public List<PestObservationResponse> search(@RequestParam(required=false) UUID deviceId,@RequestParam(required=false) UUID sectorId,
            @RequestParam(required=false) java.time.Instant from,@RequestParam(required=false) java.time.Instant to,@RequestParam(defaultValue="100") int limit) {
        return service.search(deviceId,sectorId,from,to,limit).stream().map(PestObservationResponse::from).toList();
    }
    @GetMapping("/device/{deviceId}")
    public List<PestObservationResponse> recent(@PathVariable UUID deviceId, @RequestParam(defaultValue = "20") int limit) {
        return service.recent(deviceId, limit).stream().map(PestObservationResponse::from).toList();
    }
}
