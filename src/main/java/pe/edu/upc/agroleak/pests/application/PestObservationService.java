package pe.edu.upc.agroleak.pests.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.pests.domain.model.PestObservation;
import pe.edu.upc.agroleak.pests.infrastructure.PestObservationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PestObservationService {
    private final PestObservationRepository repository;
    private final DeviceService deviceService;

    private final pe.edu.upc.agroleak.farm.application.FarmAccessService farms;
    private final org.springframework.context.ApplicationEventPublisher events;
    private final int alertCount;
    private final double alertConfidence;
    public PestObservationService(PestObservationRepository repository, DeviceService deviceService,
            pe.edu.upc.agroleak.farm.application.FarmAccessService farms, org.springframework.context.ApplicationEventPublisher events,
            @org.springframework.beans.factory.annotation.Value("${agroleak.pests.alert-min-count:5}") int alertCount,
            @org.springframework.beans.factory.annotation.Value("${agroleak.pests.alert-min-confidence:0.7}") double alertConfidence) {
        if(alertCount<1 || !Double.isFinite(alertConfidence) || alertConfidence<0 || alertConfidence>1) throw new IllegalArgumentException("Umbrales de plagas invalidos");
        this.farms=farms;this.events=events;this.alertCount=alertCount;this.alertConfidence=alertConfidence;
        this.repository = repository;
        this.deviceService = deviceService;
    }

    @Transactional
    public PestObservation create(UUID deviceId, int pestCount, double confidence, String imageUrl, Instant recordedAt) {
        return create(deviceId,pestCount,confidence,imageUrl,recordedAt,null,"UNSPECIFIED");
    }

    @Transactional
    public PestObservation create(UUID deviceId,int pestCount,double confidence,String imageUrl,Instant recordedAt,UUID sectorId,String pestType) {
        if(deviceId==null && sectorId==null) throw new IllegalArgumentException("deviceId o sectorId es obligatorio");
        if(pestCount<0 || !Double.isFinite(confidence) || confidence<0 || confidence>1) throw new IllegalArgumentException("Conteo o confianza invalido");
        Device device=deviceId==null ? null : deviceService.lockOwned(deviceId);
        UUID location=sectorId==null && device!=null ? device.getSectorId() : sectorId;
        if(location!=null) farms.requireSector(location);
        if(device!=null && sectorId!=null && !sectorId.equals(device.getSectorId())) throw new IllegalArgumentException("El dispositivo no pertenece al sector indicado");
        Instant timestamp=recordedAt==null ? Instant.now() : recordedAt;
        if(timestamp.isAfter(Instant.now())) throw new IllegalArgumentException("detectedAt no puede ser futuro");
        var observation=new PestObservation(device,pestCount,confidence,imageUrl,timestamp);
        observation.describe(location,pestType==null || pestType.isBlank() ? "UNSPECIFIED" : pestType.strip());
        observation=repository.save(observation);
        if(pestCount>=alertCount && confidence>=alertConfidence)
            events.publishEvent(new pe.edu.upc.agroleak.pests.domain.model.PestDetected(deviceId,location,observation.getPestType(),pestCount,confidence));
        return observation;
    }

    @Transactional(readOnly = true)
    public List<PestObservation> recent(UUID deviceId, int limit) {
        deviceService.get(deviceId);
        pe.edu.upc.agroleak.common.domain.TimeRange.limit(limit);
        return repository.findByDeviceIdOrderByRecordedAtDesc(deviceId, PageRequest.of(0, limit)).stream()
                .filter(p -> p.getSectorId()==null || farms.visibleMonitoringSectorIds().contains(p.getSectorId())).toList();
    }

    @Transactional(readOnly=true)
    public List<PestObservation> search(UUID deviceId,UUID sectorId,Instant from,Instant to,int limit) {
        var range=pe.edu.upc.agroleak.common.domain.TimeRange.of(from,to);
        pe.edu.upc.agroleak.common.domain.TimeRange.limit(limit);
        var ids=deviceService.visibleIds(deviceId);
        if(sectorId!=null) farms.requireMonitoringSector(sectorId);
        var sectors=sectorId==null ? farms.visibleMonitoringSectorIds() : List.of(sectorId);
        org.springframework.data.jpa.domain.Specification<PestObservation> filter=(root,q,cb) -> {
            var d=root.join("device",jakarta.persistence.criteria.JoinType.LEFT);
            var scope=cb.or(root.get("sectorId").in(sectors),cb.and(cb.isNull(root.get("sectorId")),d.get("id").in(ids)));
            var p=cb.and(scope,cb.greaterThanOrEqualTo(root.get("recordedAt"),range.from()),cb.lessThan(root.get("recordedAt"),range.to()));
            if(deviceId!=null) p=cb.and(p,cb.equal(d.get("id"),deviceId));
            if(sectorId!=null) p=cb.and(p,cb.equal(root.get("sectorId"),sectorId));
            return p;
        };
        return repository.findAll(filter,PageRequest.of(0,limit,org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC,"recordedAt","id"))).getContent();
    }
    @Transactional(readOnly=true)
    public long total(Instant from,Instant to) {
        return repository.total(deviceService.visibleIds(null),farms.visibleMonitoringSectorIds(),from,to);
    }

    @Transactional(readOnly = true)
    public Optional<PestObservation> latest(UUID deviceId) {
        deviceService.get(deviceId);
        return recent(deviceId,1).stream().findFirst();
    }
}
