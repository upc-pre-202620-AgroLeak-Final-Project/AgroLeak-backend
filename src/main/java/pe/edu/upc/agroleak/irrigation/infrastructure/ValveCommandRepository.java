package pe.edu.upc.agroleak.irrigation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommand;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommandStatus;

import java.util.Optional;
import java.util.UUID;

public interface ValveCommandRepository extends JpaRepository<ValveCommand, UUID> {
    Optional<ValveCommand> findTopByDeviceIdOrderByRequestedAtDesc(UUID deviceId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select c from ValveCommand c where c.id = :id")
    Optional<ValveCommand> findLockedById(UUID id);
    boolean existsByDeviceIdAndStatus(UUID deviceId, ValveCommandStatus status);
}
