package pe.edu.upc.agroleak.monitoring.domain;

import pe.edu.upc.agroleak.alert.domain.AlertSeverity;
import pe.edu.upc.agroleak.alert.domain.AlertType;

public record AlertCandidate(AlertType type, AlertSeverity severity, String message) {}
