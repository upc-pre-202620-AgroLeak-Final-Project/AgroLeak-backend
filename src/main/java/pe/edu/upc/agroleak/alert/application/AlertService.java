package pe.edu.upc.agroleak.alert.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.alert.domain.*;
import pe.edu.upc.agroleak.alert.infrastructure.AlertRepository;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.device.domain.Device;

import java.util.List;
import java.util.UUID;

@Service
public class AlertService {
    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Alert createIfNotActive(Device device, AlertType type, AlertSeverity severity, String message) {
        if (repository.existsByDeviceIdAndTypeAndStatus(device.getId(), type, AlertStatus.ACTIVE)) {
            return null;
        }
        return repository.save(new Alert(device, type, severity, message));
    }

    @Transactional(readOnly = true)
    public List<Alert> findByDevice(UUID deviceId, AlertStatus status) {
        if (status == null) return repository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
        return repository.findByDeviceIdAndStatusOrderByCreatedAtDesc(deviceId, status);
    }

    @Transactional
    public Alert resolve(UUID alertId) {
        Alert alert = repository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + alertId));
        alert.resolve();
        return alert;
    }

    @Transactional(readOnly = true)
    public long activeCount(UUID deviceId) {
        return repository.countByDeviceIdAndStatus(deviceId, AlertStatus.ACTIVE);
    }
}
