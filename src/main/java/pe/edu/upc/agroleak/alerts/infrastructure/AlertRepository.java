package pe.edu.upc.agroleak.alerts.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.alerts.domain.model.*;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Alert> {
    List<Alert> findByDeviceIdOrderByCreatedAtDesc(UUID deviceId);
    List<Alert> findByDeviceIdAndStatusOrderByCreatedAtDesc(UUID deviceId, AlertStatus status);
    boolean existsByDeviceIdAndTypeAndStatus(UUID deviceId, AlertType type, AlertStatus status);
    long countByDeviceIdAndStatus(UUID deviceId, AlertStatus status);
    boolean existsBySectorIdAndDeviceIsNullAndTypeAndStatusNot(UUID sectorId,AlertType type,AlertStatus status);
    boolean existsByDeviceIdAndTypeAndStatusNot(UUID deviceId, AlertType type, AlertStatus status);
    long countByDeviceIdAndStatusNot(UUID deviceId, AlertStatus status);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from Alert a where a.id = :id")
    java.util.Optional<Alert> findLockedById(UUID id);
    @org.springframework.data.jpa.repository.Query("select a.type as type, a.severity as severity, count(a) as total from Alert a left join a.device d where ((a.sectorId in :sectors) or (a.sectorId is null and d.id in :ids)) and (:deviceId is null or d.id = :deviceId) and a.status <> pe.edu.upc.agroleak.alerts.domain.model.AlertStatus.RESOLVED group by a.type,a.severity")
    List<AlertCount> countUnresolved(List<UUID> ids,List<UUID> sectors,UUID deviceId);
    interface AlertCount { AlertType getType(); AlertSeverity getSeverity(); Long getTotal(); }

}
