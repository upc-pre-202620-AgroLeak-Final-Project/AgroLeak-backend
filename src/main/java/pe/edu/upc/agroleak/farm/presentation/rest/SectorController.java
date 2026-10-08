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
@Tag(name = "Farm - Sector")
public class SectorController {
    private final FarmService service;

    public SectorController(FarmService service) {
        this.service = service;
    }

    @PostMapping("/fields/{fieldId}/sectors")
    @ResponseStatus(HttpStatus.CREATED)
    public SectorResponse create(@PathVariable UUID fieldId, @Valid @RequestBody SectorRequest r) {
        return SectorResponse.from(service.createSector(fieldId, r.name(), r.areaHectares(), r.status()));
    }

    @GetMapping("/fields/{fieldId}/sectors")
    public List<SectorResponse> list(@PathVariable UUID fieldId) {
        return service.listSectors(fieldId).stream().map(SectorResponse::from).toList();
    }

    @GetMapping("/sectors/{id}")
    public SectorResponse get(@PathVariable UUID id) {
        return SectorResponse.from(service.getSector(id));
    }

    @PutMapping("/sectors/{id}")
    public SectorResponse update(@PathVariable UUID id, @Valid @RequestBody SectorRequest r) {
        return SectorResponse.from(service.updateSector(id, r.name(), r.areaHectares(), r.status()));
    }

    @DeleteMapping("/sectors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteSector(id);
    }
}
