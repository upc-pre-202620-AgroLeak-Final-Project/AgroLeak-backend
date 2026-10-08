package pe.edu.upc.agroleak.alerts.domain.model;

public enum AlertType {
    LEAK,
    OBSTRUCTION,
    LOW_PRESSURE,
    HIGH_PRESSURE,
    DEVICE_OFFLINE,
    PEST_DETECTED,
    /** Legacy value retained for existing rows and filters. */
    PRESSURE_OUT_OF_RANGE
}
