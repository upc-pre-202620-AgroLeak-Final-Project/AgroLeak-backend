# Contratos y decisiones del bloque final

## Monitoring

`POST /api/v1/monitoring/readings` y `POST /api/v1/readings` comparten el caso de uso. Campos: `deviceId`, `sensorType`, `value`, `unit`, `recordedAt` opcional. Unidades: FLOW_IN/FLOW_OUT en `L/min`, PRESSURE en `bar`, SOIL_MOISTURE en `%`. Se rechazan valores negativos/no finitos, humedad >100, unidades incompatibles y fechas futuras.

`GET /api/v1/monitoring/readings` admite `deviceId`, `from`, `to`, `sensorType`, `limit` (100 por defecto). Sin deviceId consulta los dispositivos visibles. Orden descendente por fecha e ID, con límite explícito, no paginación ilimitada. Rangos `[from,to)` de hasta 31 días; se usan las últimas 24 horas si no se indica rango. Fechas ISO-8601 con zona, por ejemplo `2026-10-08T00:00:00Z`.

`GET /api/v1/monitoring/devices/{deviceId}/latest` devuelve la última lectura individual o null. `/latest-by-type` devuelve el mapa de lecturas por sensor. `/summary?from=...&to=...` devuelve `flowIn`, `flowOut`, `pressure`, `soilMoisture`, cada uno con `average`, `min`, `max`, `samples`. Sin muestras: métricas null y samples 0, nunca valores inventados. Los agregados se calculan en la base de datos.

## Reglas y offline

Se conservan los umbrales originales: pérdida >=20% con entrada >=3 L/min, presión fuera de 1–4 bar, obstrucción con salida <=5 L/min y presión >=3.2 bar. Las muestras usadas conjuntamente deben estar dentro de 120 segundos de la más reciente; una presión antigua no se combina con un caudal reciente. Una misma muestra puede satisfacer LEAK y OBSTRUCTION: son hipótesis de reglas, no un diagnóstico físico excluyente.

Nuevas alertas de presión: LOW_PRESSURE/HIGH_PRESSURE. PRESSURE_OUT_OF_RANGE sigue siendo un valor válido para registros y filtros históricos.

Cada 60 segundos se revisan dispositivos ONLINE cuyo lastSeen supera 300 segundos. Se bloquea y vuelve a comprobar el registro para evitar competir con una lectura entrante; pasa a OFFLINE y genera DEVICE_OFFLINE. Dispositivos nunca vistos y MAINTENANCE quedan excluidos. Una lectura nueva actualiza lastSeen y restaura ONLINE, salvo MAINTENANCE. La alerta previa requiere resolución explícita.

## Alerts

`GET /api/v1/alerts` filtra por deviceId/status/severity/type/limit. Sin deviceId devuelve incidentes visibles del usuario, incluidos incidentes de sector sin dispositivo. Severidades LOW/MEDIUM/HIGH/CRITICAL; las reglas actuales generan MEDIUM/HIGH. Los recuentos `activeAlerts` incluyen ACTIVE y ACKNOWLEDGED, pues ambos siguen sin resolver.

`PATCH /api/v1/alerts/{id}/acknowledge`: ACTIVE → ACKNOWLEDGED, registra acknowledgedAt/acknowledgedBy. `PATCH /api/v1/alerts/{id}/resolve`: ACTIVE o ACKNOWLEDGED → RESOLVED, registra resolvedAt/resolvedBy. Repetir la misma acción es idempotente y conserva sus timestamps; reconocer una alerta ya resuelta devuelve 409.

Una alerta no resuelta impide crear otra del mismo tipo y dispositivo (o del mismo sector para observaciones sin dispositivo). Los productores se serializan bloqueando la fila de origen. Reconocer no desactiva esta deduplicación. Tras resolver, un nuevo evento anómalo puede abrir otro incidente. No hay borrado ni autoresolución de incidentes históricos.

## Irrigation

Se mantienen PENDING → CONFIRMED/FAILED. No se añade EXECUTING porque el protocolo HTTP actual carece de una notificación independiente de inicio. Crear comandos requiere MANUAL y un dispositivo VALVE/GATEWAY; los dispositivos antiguos sin tipo mantienen compatibilidad.

`GET/PUT /api/v1/valves/{deviceId}/mode`, payload `{"mode":"MANUAL"}`. MANUAL es el valor por defecto para instalaciones anteriores. MONITOR_ONLY no acepta comandos. AUTO_SAFE conserva la intención de configuración pero rechaza comandos hasta implementar automatización segura. Ningún modo inicia acciones automáticamente. No puede cambiarse de modo mientras exista un comando pendiente.

Solo hay un comando pendiente por dispositivo mediante validación bajo bloqueo. La confirmación también se serializa y requiere acceso al dispositivo. Por ahora la simula un usuario autorizado; no se implementa una identidad técnica MQTT para dispositivos.

## Pests

Se conservan los nombres antiguos `pestCount`, `recordedAt`, `deviceId`. También se aceptan `count`, `detectedAt`, `cameraId` como alias. Las respuestas incluyen los nombres antiguos y `count`/`detectedAt`, además de sectorId y pestType.

Se requiere deviceId o sectorId. Si hay dispositivo, se toma su sector; un sector explícito diferente es rechazado. Las observaciones sin dispositivo pertenecen al sector indicado. El sector se registra como ubicación de la observación y no cambia si luego se reasigna el dispositivo. Un sector referenciado por observaciones/alertas no puede eliminarse mientras existan esas referencias en PostgreSQL.

```json
{"sectorId":"UUID","pestType":"APHID","count":7,"confidence":0.9,"imageUrl":"https://example.com/pest.jpg","detectedAt":"2026-10-08T00:00:00Z"}
```

`imageUrl` es opcional, debe ser HTTP(S); el backend no descarga imágenes ni ejecuta visión artificial. Count >=5 y confidence >=0.7 publica `PestDetected`, consumido por Alerts para generar PEST_DETECTED. Ambos límites son configurables. Ingesta y alerta participan en la misma transacción local; no hay broker ni entrega distribuida.

## Analytics

`GET /api/v1/analytics/dashboard` devuelve waterUsageToday, estimatedWaterLoss, activeAlerts, criticalAlerts, onlineDevices, offlineDevices, maintenanceDevices y pestsDetectedToday. El día se calcula en UTC y la respuesta lo declara junto con el intervalo. Plagas cuenta observaciones de hoy, independientemente de si superaron el umbral de alertas.

`GET /api/v1/analytics/charts?deviceId=...` devuelve 24 intervalos de una hora de las últimas 24 horas, consumo/pérdida y promedios de caudal de entrada/salida, presión y humedad. Incluye distribución de incidentes actualmente no resueltos por tipo y severidad; no representa todas las alertas históricas de esas 24 horas.

Los volúmenes son **estimaciones en litros**, no suma de valores L/min. Monitoring aplica retención constante de cada muestra hasta la siguiente o hasta 300 segundos, lo que ocurra primero; recorta en los límites temporales e incluye la última muestra previa cuando sigue vigente. La pérdida integra max(entrada−salida,0) únicamente cuando ambos caudales del mismo dispositivo tienen muestras vigentes. Una salida ausente no implica pérdida del 100%.

`coveredDeviceSeconds` y `pairedDeviceSeconds` indican cobertura acumulada por dispositivo, no segundos de reloj únicos. Las series sin datos muestran promedios null y volumen integrado 0; cero estimado no demuestra consumo físico nulo. No se rellenan huecos largos. Cada flujo debe representar un punto físico independiente: publicar la misma medición en gateway y sensor duplicaría consumo; el seed usa solamente el gateway como punto de medición.

Las consultas interactivas de Analytics se limitan a 100000 lecturas; si se excede el presupuesto devuelven 400 y solicitan reducir el rango/filtrar, nunca un total silenciosamente truncado. La base usa índices de dispositivo/sensor/fecha. No hay almacenamiento de métricas derivadas ni otro motor de reglas dentro de Analytics.

## DEV, PROD y migraciones

Ver el README para las variables y transición. DEV y PROD usan Flyway + Hibernate validate. Los tests rápidos usan H2; las pruebas PostgreSQL optativas ejecutan los scripts reales en esquemas temporales. PROD rechaza secreto vacío/corto, orígenes wildcard y conexión sin formato JDBC PostgreSQL. El seed y los controllers demo están excluidos por perfil aun si se define DEMO_DATA_ENABLED=true.

Referencias técnicas: [inicialización de base en Spring Boot](https://docs.spring.io/spring-boot/how-to/data-initialization.html) y [baseline de Flyway](https://documentation.red-gate.com/flyway/reference/commands/baseline).
