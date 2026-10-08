package pe.edu.upc.agroleak.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.devices.infrastructure.DeviceRepository;
import pe.edu.upc.agroleak.pests.application.PestObservationService;
import pe.edu.upc.agroleak.monitoring.application.ReadingService;
import pe.edu.upc.agroleak.monitoring.domain.model.SensorType;

@Component
@ConditionalOnProperty(prefix = "agroleak", name = "demo-data-enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final ReadingService readingService;
    private final PestObservationService pestService;

    public DemoDataLoader(DeviceRepository deviceRepository, DeviceService deviceService, ReadingService readingService,
                          PestObservationService pestService) {
        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
        this.readingService = readingService;
        this.pestService = pestService;
    }

    @Override
    public void run(String... args) {
        if (deviceRepository.count() > 0) return;

        Device device = deviceService.create("AgroLeak Gateway 01", "Parcela A - Sector Norte");
        readingService.create(device.getId(), SensorType.FLOW_IN, 25.0, "L/min", null);
        readingService.create(device.getId(), SensorType.FLOW_OUT, 24.2, "L/min", null);
        readingService.create(device.getId(), SensorType.PRESSURE, 2.2, "bar", null);
        readingService.create(device.getId(), SensorType.SOIL_MOISTURE, 48.0, "%", null);
        pestService.create(device.getId(), 2, 0.82, null, null);

        log.info("AgroLeak demo device created. deviceId={}", device.getId());
    }
}
