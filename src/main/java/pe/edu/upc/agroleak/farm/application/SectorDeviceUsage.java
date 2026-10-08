package pe.edu.upc.agroleak.farm.application;

import java.util.UUID;

/**
 * Port implemented by devices; no aggregate or persistence relationship crosses contexts.
 */
public interface SectorDeviceUsage {
    boolean hasDevices(UUID sectorId);
}
