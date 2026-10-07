package pe.edu.upc.agroleak.alert.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.alert.domain.*;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);
    List<Alert> findByDeviceIdAndStatusOrderByCreatedAtDesc(UUID deviceId, AlertStatus status);
    boolean existsByDeviceIdAndTypeAndStatus(UUID deviceId, AlertType type, AlertStatus status);
    long countByDeviceIdAndStatus(UUID deviceId, AlertStatus status);
}
