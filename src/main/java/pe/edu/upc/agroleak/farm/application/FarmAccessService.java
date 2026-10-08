package pe.edu.upc.agroleak.farm.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import pe.edu.upc.agroleak.iam.application.CurrentUser;
import pe.edu.upc.agroleak.farm.domain.repository.*;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FarmAccessService {
    private final FarmRepository farms;
    private final FieldRepository fields;
    private final SectorRepository sectors;
    private final CurrentUser current;

    public FarmAccessService(FarmRepository farms, FieldRepository fields, SectorRepository sectors, CurrentUser current) {
        this.farms = farms;
        this.fields = fields;
        this.sectors = sectors;
        this.current = current;
    }

    public void requireFarm(UUID id) {
        var farm = farms.findById(id).orElseThrow(() -> new ResourceNotFoundException("Farm no encontrada"));
        if (!current.isAdmin() && !farm.getOwnerId().equals(current.id()))
            throw new AccessDeniedException("Farm ajena");
    }

    public void requireField(UUID id) {
        var field = fields.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field no encontrado"));
        requireFarm(field.getFarmId());
    }

    public void requireSector(UUID id) {
        var sector = sectors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado"));
        requireField(sector.getFieldId());
    }

    @Transactional
    public void lockSector(UUID id) {
        sectors.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado"));
        requireSector(id);
    }
    public java.util.List<UUID> visibleMonitoringSectorIds() {
        return sectors.findAll().stream().filter(s -> current.isAdmin() || current.isTechnician() || ownsSector(s.getId()))
                .map(pe.edu.upc.agroleak.farm.domain.model.Sector::getId).toList();
    }
    public void requireMonitoringSector(UUID id) {
        sectors.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sector no encontrado"));
        if (!current.isAdmin() && !current.isTechnician()) requireSector(id);
    }

    public boolean ownsSector(UUID id) {
        try {
            requireSector(id);
            return true;
        } catch (AccessDeniedException | ResourceNotFoundException ex) {
            return false;
        }
    }

    public void requireManager() {
        if (current.isTechnician() && !current.isAdmin()) throw new AccessDeniedException("Se requiere FARMER o ADMIN");
    }
}
