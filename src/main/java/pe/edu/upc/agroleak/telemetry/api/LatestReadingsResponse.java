package pe.edu.upc.agroleak.telemetry.api;

import java.util.Map;

public record LatestReadingsResponse(Map<String, ReadingResponse> readings) {}
