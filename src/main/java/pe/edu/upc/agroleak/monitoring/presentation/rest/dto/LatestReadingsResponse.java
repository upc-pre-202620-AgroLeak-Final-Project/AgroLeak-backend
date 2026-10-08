package pe.edu.upc.agroleak.monitoring.presentation.rest.dto;

import java.util.Map;

public record LatestReadingsResponse(Map<String, ReadingResponse> readings) {}
