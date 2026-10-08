package pe.edu.upc.agroleak.irrigation.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upc.agroleak.common.exception.BusinessRuleException;
import pe.edu.upc.agroleak.common.exception.ResourceNotFoundException;
import pe.edu.upc.agroleak.devices.application.DeviceService;
import pe.edu.upc.agroleak.devices.domain.model.Device;
import pe.edu.upc.agroleak.irrigation.domain.model.*;
import pe.edu.upc.agroleak.irrigation.infrastructure.ValveCommandRepository;

import java.util.Optional;
import java.util.UUID;

@Service
public class ValveService {
    private final ValveCommandRepository repository;
    private final DeviceService deviceService;

    private final pe.edu.upc.agroleak.irrigation.infrastructure.IrrigationSettingsRepository settings;
    public ValveService(ValveCommandRepository repository, DeviceService deviceService,
                        pe.edu.upc.agroleak.irrigation.infrastructure.IrrigationSettingsRepository settings) {
        this.settings=settings;
        this.repository = repository;
        this.deviceService = deviceService;
    }

    @Transactional
    public ValveCommand request(UUID deviceId, ValveAction action) {
        Device device = deviceService.lockOwned(deviceId);
        var type=device.getDeviceType();
        if(type!=null && type!=pe.edu.upc.agroleak.devices.domain.model.DeviceType.VALVE && type!=pe.edu.upc.agroleak.devices.domain.model.DeviceType.GATEWAY)
            throw new BusinessRuleException("El dispositivo no controla una valvula");
        if (mode(deviceId) != OperationMode.MANUAL) throw new BusinessRuleException("Solo MANUAL permite comandos; AUTO_SAFE aun no esta implementado");
        if (repository.existsByDeviceIdAndStatus(deviceId, ValveCommandStatus.PENDING)) {
            throw new BusinessRuleException("Ya existe un comando de válvula pendiente para este dispositivo");
        }
        return repository.save(new ValveCommand(device, action));
    }

    @Transactional
    public ValveCommand confirm(UUID commandId, boolean success) {
        ValveCommand command = repository.findLockedById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("Comando de válvula no encontrado: " + commandId));
        deviceService.get(command.getDevice().getId());
        if (command.getStatus() != ValveCommandStatus.PENDING) {
            throw new BusinessRuleException("El comando ya fue procesado");
        }
        command.confirm(success);
        return command;
    }

    @Transactional(readOnly=true)
    public OperationMode mode(UUID deviceId) {
        deviceService.get(deviceId);
        return settings.findById(deviceId).map(IrrigationSettings::getMode).orElse(OperationMode.MANUAL);
    }
    @Transactional
    public OperationMode changeMode(UUID deviceId,OperationMode mode) {
        deviceService.lockOwned(deviceId);
        if(repository.existsByDeviceIdAndStatus(deviceId,ValveCommandStatus.PENDING)) throw new BusinessRuleException("Confirme el comando pendiente antes de cambiar modo");
        var config=settings.findById(deviceId).orElseGet(() -> new IrrigationSettings(deviceId,mode));
        config.changeMode(mode); settings.save(config); return mode;
    }

    @Transactional(readOnly = true)
    public Optional<ValveCommand> latest(UUID deviceId) {
        deviceService.get(deviceId);
        return repository.findTopByDeviceIdOrderByRequestedAtDesc(deviceId);
    }
}
