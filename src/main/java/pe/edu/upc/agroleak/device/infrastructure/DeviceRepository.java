package pe.edu.upc.agroleak.device.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.device.domain.Device;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
}
