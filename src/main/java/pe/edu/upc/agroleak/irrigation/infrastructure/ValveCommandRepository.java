package pe.edu.upc.agroleak.irrigation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommand;
import pe.edu.upc.agroleak.irrigation.domain.model.ValveCommandStatus;

import java.util.Optional;
import java.util.UUID;

public interface ValveCommandRepository extends JpaRepository<ValveCommand, UUID> {
    Optional<ValveCommand> findTopByDeviceIdOrderByRequestedAtDesc(UUID deviceId);
    boolean existsByDeviceIdAndStatus(UUID deviceId, ValveCommandStatus status);
}
