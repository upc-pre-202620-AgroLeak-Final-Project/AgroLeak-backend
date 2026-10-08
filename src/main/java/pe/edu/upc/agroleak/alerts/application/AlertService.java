package pe.edu.upc.agroleak.alerts.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.alerts.domain.model.*;
import pe.edu.upc.agroleak.alerts.infrastructure.AlertRepository;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.devices.domain.model.Device;

import java.util.List;
import java.util.UUID;

@Service
public class AlertService {
    private final AlertRepository repository;
    private final pe.edu.upc.agroleak.devices.application.DeviceService devices;

    private final pe.edu.upc.agroleak.farm.application.FarmAccessService farms;
    private final pe.edu.upc.agroleak.iam.application.CurrentUser current;
    private final pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository deviceRepository;
    public AlertService(AlertRepository repository, pe.edu.upc.agroleak.devices.application.DeviceService devices,
                        pe.edu.upc.agroleak.iam.application.CurrentUser current,
                        pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository deviceRepository,
                        pe.edu.upc.agroleak.farm.application.FarmAccessService farms) {
        this.farms=farms;
        this.current=current;
        this.deviceRepository=deviceRepository;
        this.repository = repository;
        this.devices = devices;
    }

    private void checkAccess(Alert alert) {
        if(alert.getSectorId()!=null) farms.requireMonitoringSector(alert.getSectorId());
        else devices.get(alert.getDevice().getId());
    }
    @org.springframework.context.event.EventListener
    @Transactional
    public void onPestDetected(pe.edu.upc.agroleak.pests.domain.model.PestDetected event) {
        String message="Plaga " + event.pestType() + ": " + event.count() + " detecciones";
        if(event.deviceId()!=null) {
            createIfNotActive(devices.get(event.deviceId()),AlertType.PEST_DETECTED,AlertSeverity.HIGH,message);
        } else {
            // Serialize sector-only producers on their aggregate row without a cross-context JPA relationship.
            farms.lockSector(event.sectorId());
            if(repository.existsBySectorIdAndDeviceIsNullAndTypeAndStatusNot(event.sectorId(),AlertType.PEST_DETECTED,AlertStatus.RESOLVED)) return;
            var alert=new Alert(null,AlertType.PEST_DETECTED,AlertSeverity.HIGH,message);
            alert.locate(event.sectorId()); repository.save(alert);
        }
    }

    @Transactional
    public Alert createIfNotActive(Device device, AlertType type, AlertSeverity severity, String message) {
        // Serialize producers per device to avoid duplicate open incidents, including ACKNOWLEDGED ones.
        deviceRepository.findLockedById(device.getId()).orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado"));
        if (repository.existsByDeviceIdAndTypeAndStatusNot(device.getId(), type, AlertStatus.RESOLVED)) {
            return null;
        }
        return repository.save(new Alert(device, type, severity, message));
    }

    @Transactional(readOnly = true)
    public List<Alert> findByDevice(UUID deviceId, AlertStatus status) {
        return search(deviceId,status,null,null,1000);
    }

    @Transactional
    public Alert resolve(UUID alertId) {
        Alert alert = repository.findLockedById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + alertId));
        checkAccess(alert);
        alert.resolve(current.id());
        return alert;
    }

    @Transactional
    public Alert acknowledge(UUID id) {
        var alert=repository.findLockedById(id).orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada"));
        checkAccess(alert);
        alert.acknowledge(current.id());
        return alert;
    }

    @Transactional(readOnly=true)
    public List<Alert> search(UUID deviceId, AlertStatus status, AlertSeverity severity, AlertType type, int limit) {
        pe.edu.upc.agroleak.common.domain.TimeRange.limit(limit);
        var ids=devices.visibleIds(deviceId);
        var sectors=farms.visibleMonitoringSectorIds();
        org.springframework.data.jpa.domain.Specification<Alert> filter=(root,q,cb) -> {
            var p=new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            var d=root.join("device",jakarta.persistence.criteria.JoinType.LEFT);
            p.add(cb.or(root.get("sectorId").in(sectors),cb.and(cb.isNull(root.get("sectorId")),d.get("id").in(ids))));
            if(deviceId!=null) p.add(cb.equal(d.get("id"),deviceId));
            if(status!=null) p.add(cb.equal(root.get("status"),status));
            if(severity!=null) p.add(cb.equal(root.get("severity"),severity));
            if(type!=null) p.add(cb.equal(root.get("type"),type));
            return cb.and(p.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
        return repository.findAll(filter,org.springframework.data.domain.PageRequest.of(0,limit,org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"createdAt","id"))).getContent();
    }

    public record Overview(long activeAlerts,long criticalAlerts,java.util.Map<AlertType,Long> byType,java.util.Map<AlertSeverity,Long> bySeverity) {}
    @Transactional(readOnly=true)
    public Overview overview(UUID deviceId) {
        var ids=devices.visibleIds(deviceId);
        java.util.Map<AlertType,Long> types=new java.util.EnumMap<>(AlertType.class);
        java.util.Map<AlertSeverity,Long> severities=new java.util.EnumMap<>(AlertSeverity.class);
        for(var t:AlertType.values()) types.put(t,0L);
        for(var s:AlertSeverity.values()) severities.put(s,0L);
        repository.countUnresolved(ids,farms.visibleMonitoringSectorIds(),deviceId).forEach(c -> {
            types.merge(c.getType(),c.getTotal(),Long::sum); severities.merge(c.getSeverity(),c.getTotal(),Long::sum);
        });
        return new Overview(types.values().stream().mapToLong(Long::longValue).sum(),severities.get(AlertSeverity.CRITICAL),types,severities);
    }

    @Transactional(readOnly = true)
    public long activeCount(UUID deviceId) {
        devices.get(deviceId);
        return overview(deviceId).activeAlerts();
    }
}
