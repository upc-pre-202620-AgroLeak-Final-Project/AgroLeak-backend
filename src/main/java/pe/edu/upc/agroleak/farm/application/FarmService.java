package pe.edu.upc.agroleak.farm.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.farm.domain.model.*;
import pe.edu.upc.agroleak.farm.domain.repository.*;
import pe.edu.upc.agroleak.iam.application.CurrentUser;
import pe.edu.upc.agroleak.common.exception.*;

import java.util.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class FarmService {
    private final FarmRepository farms;
    private final FieldRepository fields;
    private final SectorRepository sectors;
    private final CropRepository crops;
    private final CurrentUser current;
    private final FarmAccessService access;
    private final SectorDeviceUsage devices;

    public FarmService(FarmRepository farms, FieldRepository fields, SectorRepository sectors, CropRepository crops,
                       CurrentUser current, FarmAccessService access, SectorDeviceUsage devices) {
        this.farms = farms;
        this.fields = fields;
        this.sectors = sectors;
        this.crops = crops;
        this.current = current;
        this.access = access;
        this.devices = devices;
    }

    public List<Farm> listFarms() {
        return current.isAdmin() ? farms.findAll() : farms.findByOwnerId(current.id());
    }

    public Farm getFarm(UUID id) {
        var entity = farms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Farm no encontrado"));
        access.requireFarm(id);
        return entity;
    }

    @Transactional
    public Farm createFarm(String name, String location, BigDecimal areaHectares) {
        access.requireManager();
        return farms.save(new Farm(name, location, areaHectares, current.id()));
    }

    @Transactional
    public Farm updateFarm(UUID id, String name, String location, BigDecimal areaHectares) {
        access.requireManager();
        var entity = getFarm(id);
        entity.update(name, location, areaHectares);
        return farms.save(entity);
    }

    @Transactional
    public void deleteFarm(UUID id) {
        access.requireManager();
        var entity = getFarm(id);
        if (fields.existsByFarmId(id))
            throw new BusinessRuleException("Elimine o reasigne los recursos dependientes primero");
        farms.delete(entity);
    }

    public Field getField(UUID id) {
        var entity = fields.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field no encontrado"));
        access.requireField(id);
        return entity;
    }

    public List<Field> listFields(UUID farmId) {
        access.requireFarm(farmId);
        return fields.findByFarmId(farmId);
    }

    @Transactional
    public Field createField(UUID farmId, String name, BigDecimal areaHectares, String description) {
        access.requireManager();
        access.requireFarm(farmId);
        return fields.save(new Field(farmId, name, areaHectares, description));
    }

    @Transactional
    public Field updateField(UUID id, String name, BigDecimal areaHectares, String description) {
        access.requireManager();
        var entity = getField(id);
        entity.update(name, areaHectares, description);
        return fields.save(entity);
    }

    @Transactional
    public void deleteField(UUID id) {
        access.requireManager();
        var entity = getField(id);
        if (sectors.existsByFieldId(id))
            throw new BusinessRuleException("Elimine o reasigne los recursos dependientes primero");
        fields.delete(entity);
    }

    public Sector getSector(UUID id) {
        var entity = sectors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado"));
        access.requireSector(id);
        return entity;
    }

    public List<Sector> listSectors(UUID fieldId) {
        access.requireField(fieldId);
        return sectors.findByFieldId(fieldId);
    }

    @Transactional
    public Sector createSector(UUID fieldId, String name, BigDecimal areaHectares, SectorStatus status) {
        access.requireManager();
        access.requireField(fieldId);
        return sectors.save(new Sector(fieldId, name, areaHectares, status));
    }

    @Transactional
    public Sector updateSector(UUID id, String name, BigDecimal areaHectares, SectorStatus status) {
        access.requireManager();
        var entity = getSector(id);
        entity.update(name, areaHectares, status);
        return sectors.save(entity);
    }

    @Transactional
    public void deleteSector(UUID id) {
        access.requireManager();
        var entity = getSector(id);
        if (crops.existsBySectorId(id) || devices.hasDevices(id))
            throw new BusinessRuleException("Elimine o reasigne los recursos dependientes primero");
        sectors.delete(entity);
    }

    public Crop getCrop(UUID id) {
        var entity = crops.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crop no encontrado"));
        access.requireSector(entity.getSectorId());
        return entity;
    }

    public List<Crop> listCrops(UUID sectorId) {
        access.requireSector(sectorId);
        return crops.findBySectorId(sectorId);
    }

    @Transactional
    public Crop createCrop(UUID sectorId, String name, String variety, LocalDate plantedAt, CropStatus status) {
        access.requireManager();
        access.requireSector(sectorId);
        return crops.save(new Crop(sectorId, name, variety, plantedAt, status));
    }

    @Transactional
    public Crop updateCrop(UUID id, String name, String variety, LocalDate plantedAt, CropStatus status) {
        access.requireManager();
        var entity = getCrop(id);
        entity.update(name, variety, plantedAt, status);
        return crops.save(entity);
    }

    @Transactional
    public void deleteCrop(UUID id) {
        access.requireManager();
        var entity = getCrop(id);
        crops.delete(entity);
    }
}
