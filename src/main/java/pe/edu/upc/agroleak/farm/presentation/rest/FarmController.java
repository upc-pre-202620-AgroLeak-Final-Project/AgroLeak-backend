package pe.edu.upc.agroleak.farm.presentation.rest;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import pe.edu.upc.agroleak.farm.application.FarmService;
import pe.edu.upc.agroleak.farm.presentation.rest.dto.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Farm - Farm")
public class FarmController {
    private final FarmService service;

    public FarmController(FarmService service) {
        this.service = service;
    }

    @PostMapping("/farms")
    @ResponseStatus(HttpStatus.CREATED)
    public FarmResponse create(@Valid @RequestBody FarmRequest r) {
        return FarmResponse.from(service.createFarm(r.name(), r.location(), r.areaHectares()));
    }

    @GetMapping("/farms")
    public List<FarmResponse> list() {
        return service.listFarms().stream().map(FarmResponse::from).toList();
    }

    @GetMapping("/farms/{id}")
    public FarmResponse get(@PathVariable UUID id) {
        return FarmResponse.from(service.getFarm(id));
    }

    @PutMapping("/farms/{id}")
    public FarmResponse update(@PathVariable UUID id, @Valid @RequestBody FarmRequest r) {
        return FarmResponse.from(service.updateFarm(id, r.name(), r.location(), r.areaHectares()));
    }

    @DeleteMapping("/farms/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteFarm(id);
    }
}
