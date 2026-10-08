package pe.edu.upc.agroleak.devices.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upc.agroleak.devices.domain.model.Device;

import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
}
