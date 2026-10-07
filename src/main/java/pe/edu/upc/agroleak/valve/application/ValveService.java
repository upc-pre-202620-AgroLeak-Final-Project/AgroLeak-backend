package pe.edu.upc.agroleak.valve.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.common.exception.BusinessRuleException;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.device.application.DeviceService;
import pe.edu.upc.agroleak.device.domain.Device;
import pe.edu.upc.agroleak.valve.domain.*;
import pe.edu.upc.agroleak.valve.infrastructure.ValveCommandRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class ValveService {
    private final ValveCommandRepository repository;
    private final DeviceService deviceService;

    public ValveService(ValveCommandRepository repository, DeviceService deviceService) {
        this.repository = repository;
        this.deviceService = deviceService;
    }

    @Transactional
    public ValveCommand request(UUID deviceId, ValveAction action) {
        Device device = deviceService.get(deviceId);
        if (repository.existsByDeviceIdAndStatus(deviceId, ValveCommandStatus.PENDING)) {
            throw new BusinessRuleException("Ya existe un comando de válvula pendiente para este dispositivo");
        }
        return repository.save(new ValveCommand(device, action));
    }

    @Transactional
    public ValveCommand confirm(UUID commandId, boolean success) {
        ValveCommand command = repository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("Comando de válvula no encontrado: " + commandId));
        if (command.getStatus() != ValveCommandStatus.PENDING) {
            throw new BusinessRuleException("El comando ya fue procesado");
        }
        command.confirm(success);
        return command;
    }

    @Transactional(readOnly = true)
    public Optional<ValveCommand> latest(UUID deviceId) {
        deviceService.get(deviceId);
        return repository.findTopByDeviceIdOrderByRequestedAtDesc(deviceId);
    }
}
