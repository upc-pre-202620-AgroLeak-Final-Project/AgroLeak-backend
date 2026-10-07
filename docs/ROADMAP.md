# AgroLeak — Roadmap técnico

## v0.1 — Entrega parcial

**Objetivo:** probar el flujo IoT end-to-end con el mínimo de complejidad razonable.

- Spring Boot REST API.
- PostgreSQL.
- Device management básico.
- Telemetría de flujo, presión y humedad.
- Reglas deterministas.
- Alertas.
- Comandos manuales de válvula con confirmación.
- Pest observations como contrato de integración.
- Dashboard agregado.
- Swagger.
- Tests.

## v0.2 — Dispositivo IoT real

- Integración ESP32/Wokwi.
- MQTT broker.
- topic convention por deviceId.
- heartbeat y estado offline.
- reintentos/reconexión.
- timestamps originados en dispositivo.

## v0.3 — Tiempo real

- SSE o WebSockets para frontend.
- gráficos de series temporales.
- actualización de estado sin polling agresivo.
- histórico por rango de fechas.

## v0.4 — Comunicación de alertas

- email.
- WhatsApp/push como canal posterior.
- preferencias de notificación.
- acknowledgement de alerta.

## v0.5 — Automatización segura

Modos operativos:

1. `MONITOR_ONLY`
2. `MANUAL_APPROVAL`
3. `SAFE_AUTO`

Agregar:

- reglas de autorización.
- timeout de comando.
- feedback físico de válvula.
- auditoría de acciones.
- fail-safe.

## v0.6 — Anomaly Detection

- dataset histórico.
- baseline por sector/cultivo/hora.
- scoring de anomalía.
- comparación contra reglas v0.1.
- versionado del modelo.

## v0.7 — Computer Vision

- captura de frames.
- servicio de inferencia.
- detección/conteo de plagas.
- almacenamiento de metadata y evidencia.
- revisión humana.

## v1.0 — Producto escalable

- autenticación y JWT/OAuth.
- usuarios, organizaciones y roles.
- fundos, sectores y múltiples gateways.
- configuración de umbrales por sector.
- PostgreSQL gestionado/cloud.
- observabilidad.
- CI/CD.
- seguridad de dispositivo/API key o certificados.
- mobile application.
