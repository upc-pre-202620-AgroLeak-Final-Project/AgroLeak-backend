package pe.edu.upc.agroleak.devices.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.devices.domain.model.Device;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    boolean existsBySectorId(UUID sectorId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Device d where d.id = :id")
    java.util.Optional<Device> findLockedById(UUID id);
    @org.springframework.data.jpa.repository.Query("select d.id from Device d where d.status = pe.edu.upc.agroleak.devices.domain.model.DeviceStatus.ONLINE and d.lastSeen < :cutoff")
    java.util.List<UUID> findStaleOnline(java.time.Instant cutoff);

}
