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
@Tag(name = "Farm - Field")
public class FieldController {
    private final FarmService service;

    public FieldController(FarmService service) {
        this.service = service;
    }

    @PostMapping("/farms/{farmId}/fields")
    @ResponseStatus(HttpStatus.CREATED)
    public FieldResponse create(@PathVariable UUID farmId, @Valid @RequestBody FieldRequest r) {
        return FieldResponse.from(service.createField(farmId, r.name(), r.areaHectares(), r.description()));
    }

    @GetMapping("/farms/{farmId}/fields")
    public List<FieldResponse> list(@PathVariable UUID farmId) {
        return service.listFields(farmId).stream().map(FieldResponse::from).toList();
    }

    @GetMapping("/fields/{id}")
    public FieldResponse get(@PathVariable UUID id) {
        return FieldResponse.from(service.getField(id));
    }

    @PutMapping("/fields/{id}")
    public FieldResponse update(@PathVariable UUID id, @Valid @RequestBody FieldRequest r) {
        return FieldResponse.from(service.updateField(id, r.name(), r.areaHectares(), r.description()));
    }

    @DeleteMapping("/fields/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteField(id);
    }
}
