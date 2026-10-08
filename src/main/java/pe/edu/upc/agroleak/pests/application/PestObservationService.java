package pe.edu.upc.agroleak.pests.application;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.pests.domain.model.PestObservation;
import pe.edu.upc.agroleak.pests.infrastructure.PestObservationRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PestObservationService {
    private final PestObservationRepository repository;
    private final DeviceService deviceService;

    public PestObservationService(PestObservationRepository repository, DeviceService deviceService) {
        this.repository = repository;
        this.deviceService = deviceService;
    }

    @Transactional
    public PestObservation create(UUID deviceId, int pestCount, double confidence, String imageUrl, Instant recordedAt) {
        Device device = deviceService.get(deviceId);
        Instant timestamp = recordedAt == null ? Instant.now() : recordedAt;
        return repository.save(new PestObservation(device, pestCount, confidence, imageUrl, timestamp));
    }

    @Transactional(readOnly = true)
    public List<PestObservation> recent(UUID deviceId, int limit) {
        deviceService.get(deviceId);
        return repository.findByDeviceIdOrderByRecordedAtDesc(deviceId, PageRequest.of(0, Math.max(1, Math.min(limit, 100))));
    }

    @Transactional(readOnly = true)
    public Optional<PestObservation> latest(UUID deviceId) {
        return repository.findTopByDeviceIdOrderByRecordedAtDesc(deviceId);
    }
}
