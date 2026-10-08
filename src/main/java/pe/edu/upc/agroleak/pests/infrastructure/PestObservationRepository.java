package pe.edu.upc.agroleak.pests.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.pests.domain.model.PestObservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PestObservationRepository extends JpaRepository<PestObservation, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<PestObservation> {
    @org.springframework.data.jpa.repository.Query("select coalesce(sum(p.pestCount),0) from PestObservation p left join p.device d where ((p.sectorId in :sectors) or (p.sectorId is null and d.id in :ids)) and p.recordedAt >= :from and p.recordedAt < :to")
    long total(java.util.List<UUID> ids,java.util.List<UUID> sectors,java.time.Instant from,java.time.Instant to);
    List<PestObservation> findByDeviceIdOrderByRecordedAtDesc(UUID deviceId, Pageable pageable);
    Optional<PestObservation> findTopByDeviceIdOrderByRecordedAtDesc(UUID deviceId);
}
