package pe.edu.upc.agroleak.pest.infrastructure;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.pest.domain.PestObservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PestObservationRepository extends JpaRepository<PestObservation, UUID> {
    List<PestObservation> findByDeviceIdOrderByRecordedAtDesc(UUID deviceId, Pageable pageable);
    Optional<PestObservation> findTopByDeviceIdOrderByRecordedAtDesc(UUID deviceId);
}
