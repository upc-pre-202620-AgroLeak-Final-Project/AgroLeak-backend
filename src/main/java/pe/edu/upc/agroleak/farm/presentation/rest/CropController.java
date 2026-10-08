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
@Tag(name = "Farm - Crop")
public class CropController {
    private final FarmService service;

    public CropController(FarmService service) {
        this.service = service;
    }

    @PostMapping("/sectors/{sectorId}/crops")
    @ResponseStatus(HttpStatus.CREATED)
    public CropResponse create(@PathVariable UUID sectorId, @Valid @RequestBody CropRequest r) {
        return CropResponse.from(service.createCrop(sectorId, r.name(), r.variety(), r.plantedAt(), r.status()));
    }

    @GetMapping("/sectors/{sectorId}/crops")
    public List<CropResponse> list(@PathVariable UUID sectorId) {
        return service.listCrops(sectorId).stream().map(CropResponse::from).toList();
    }

    @GetMapping("/crops/{id}")
    public CropResponse get(@PathVariable UUID id) {
        return CropResponse.from(service.getCrop(id));
    }

    @PutMapping("/crops/{id}")
    public CropResponse update(@PathVariable UUID id, @Valid @RequestBody CropRequest r) {
        return CropResponse.from(service.updateCrop(id, r.name(), r.variety(), r.plantedAt(), r.status()));
    }

    @DeleteMapping("/crops/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.deleteCrop(id);
    }
}
