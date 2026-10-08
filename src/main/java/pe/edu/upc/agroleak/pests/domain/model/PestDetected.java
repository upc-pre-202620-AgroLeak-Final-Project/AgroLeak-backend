package pe.edu.upc.agroleak.pests.domain.model;
import java.util.UUID;
/** Synchronous application event; no reference to an Alerts aggregate. */
public record PestDetected(UUID deviceId, UUID sectorId, String pestType, int count, double confidence) {}
