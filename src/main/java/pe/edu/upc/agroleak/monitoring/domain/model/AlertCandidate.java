package pe.edu.upc.agroleak.monitoring.domain.model;

import pe.edu.upc.agroleak.alerts.domain.model.AlertSeverity;
import pe.edu.upc.agroleak.alerts.domain.model.AlertType;

public record AlertCandidate(AlertType type, AlertSeverity severity, String message) {}
