# AgroLeak Backend v0.1

Backend académico del MVP **AgroLeak**, desarrollado para el proyecto IoT de UPC.

Esta primera versión busca demostrar un flujo IoT end-to-end suficientemente sólido para una entrega parcial, sin sobredimensionar el proyecto con microservicios, machine learning o mensajería distribuida desde el inicio.

## Alcance de esta versión

Incluye:

- API REST con Java 21 + Spring Boot 3.5.
- PostgreSQL como base de datos principal.
- Estructura de **modular monolith por bounded contexts**.
- Registro de dispositivos/gateways.
- Ingesta de telemetría de:
  - caudal de entrada;
  - caudal de salida;
  - presión;
  - humedad de suelo.
- Motor de reglas para:
  - posible fuga;
  - posible obstrucción;
  - presión fuera de rango.
- Gestión de alertas activas y resueltas.
- Control manual de válvula mediante comandos OPEN/CLOSE.
- Confirmación del comando por parte del dispositivo.
- Registro de observaciones de plagas (resultado simulado/externo de visión artificial).
- Endpoint agregado para dashboard.
- Datos y escenarios de demostración.
- Swagger/OpenAPI.
- Actuator Health.
- Pruebas unitarias e integración.
- CORS preparado para conectar el frontend.

## Fuera de alcance de v0.1

Se deja para iteraciones posteriores:

- MQTT.
- WebSockets/SSE.
- JWT y usuarios multi-tenant.
- Computer Vision real.
- ML de anomalías.
- notificaciones WhatsApp/push.
- automatización autónoma de válvula.
- aplicación móvil.
- microservicios.

## Arquitectura

```text
ESP32 / Wokwi / Postman
          |
          | HTTP/JSON
          v
+-----------------------------+
|      AgroLeak Backend       |
|  Spring Boot Modular Mono.  |
|-----------------------------|
| devices                     |
| monitoring                  |
| alerts                      |
| irrigation                  |
| pests                       |
| analytics                   |
+--------------+--------------+
               |
               v
          PostgreSQL
               |
               v
        Frontend AgroLeak
```

Cada contexto utiliza `application`, `domain/model`, `infrastructure` y `presentation/rest` cuando corresponde. Los DTO HTTP se encuentran en `presentation/rest/dto`. El contexto `monitoring` reúne la adquisición y persistencia de lecturas con la evaluación de reglas; `demo` conserva la infraestructura de demostración.

La reorganización de paquetes conserva las rutas REST y los contratos JSON existentes. Ver el detalle en [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

## Requisitos

- Java 21
- Maven 3.9+
- Docker Desktop (recomendado para PostgreSQL)

## 1. Levantar PostgreSQL

Desde la raíz:

```bash
docker compose up -d
```

Configuración por defecto:

```text
DB:       agroleak
User:     agroleak
Password: agroleak
Port:     5433 (host; 5432 dentro del contenedor)
```

## 2. Ejecutar backend

```bash
mvn spring-boot:run
```

API:

```text
http://localhost:8081
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

Health check:

```text
http://localhost:8081/actuator/health
```

## Datos demo

En el primer inicio, si la base de datos está vacía, se crea:

- 1 gateway AgroLeak.
- lecturas normales de caudal, presión y humedad.
- 1 observación de plaga.

Consulta el UUID creado mediante:

```http
GET /api/v1/devices
```

También se imprime el `deviceId` en consola.

## Escenarios para exposición

Con `DEMO_DATA_ENABLED=true`:

```http
POST /api/v1/demo/scenarios/normal/{deviceId}
POST /api/v1/demo/scenarios/leak/{deviceId}
POST /api/v1/demo/scenarios/obstruction/{deviceId}
```

Ejemplo de demostración:

1. Ejecutar escenario normal.
2. Consultar `/api/v1/dashboard/{deviceId}`.
3. Ejecutar escenario leak.
4. Consultar alertas activas.
5. Crear comando CLOSE.
6. Simular confirmación del dispositivo.
7. Mostrar el dashboard actualizado.

## Principales endpoints

### Devices

```http
POST /api/v1/devices
GET  /api/v1/devices
GET  /api/v1/devices/{id}
```

### Telemetry

```http
POST /api/v1/readings
GET  /api/v1/readings/device/{deviceId}?limit=50
GET  /api/v1/readings/device/{deviceId}/latest
```

Ejemplo:

```json
{
  "deviceId": "UUID",
  "sensorType": "FLOW_IN",
  "value": 25.4,
  "unit": "L/min"
}
```

Tipos de sensor:

```text
FLOW_IN
FLOW_OUT
PRESSURE
SOIL_MOISTURE
```

### Alerts

```http
GET   /api/v1/alerts?deviceId={id}
GET   /api/v1/alerts?deviceId={id}&status=ACTIVE
PATCH /api/v1/alerts/{alertId}/resolve
```

### Valve

```http
POST  /api/v1/valves/{deviceId}/commands
GET   /api/v1/valves/{deviceId}/latest
PATCH /api/v1/valves/commands/{commandId}/confirm
```

Crear comando:

```json
{
  "action": "CLOSE"
}
```

Confirmación simulada por ESP32:

```json
{
  "success": true
}
```

### Pest observations

```http
POST /api/v1/pest-observations
GET  /api/v1/pest-observations/device/{deviceId}?limit=20
```

Ejemplo:

```json
{
  "deviceId": "UUID",
  "pestCount": 7,
  "confidence": 0.87,
  "imageUrl": "https://example.com/frame.jpg"
}
```

> En v0.1 el backend recibe el resultado del análisis. La detección visual real se implementará en una iteración futura.

### Dashboard

```http
GET /api/v1/dashboard/{deviceId}
```

Devuelve en una sola petición:

- estado del gateway;
- últimas variables de riego;
- pérdida estimada de caudal;
- cantidad de alertas activas;
- último comando de válvula;
- última observación de plagas.

## Reglas de detección v0.1

Los umbrales son configurables en `application.yml`.

### Fuga

```text
lossPercent = (FLOW_IN - FLOW_OUT) / FLOW_IN * 100
```

Si la pérdida es >= 20% y existe flujo suficiente, se crea una alerta `LEAK` de severidad `HIGH`.

### Obstrucción

Se considera posible obstrucción cuando:

```text
FLOW_OUT <= 5 L/min
AND
PRESSURE >= 3.2 bar
```

### Presión fuera de rango

Rango MVP:

```text
1.0 bar <= PRESSURE <= 4.0 bar
```

> Estos valores son umbrales de demostración y deben calibrarse con pruebas físicas del prototipo.

## Variables de entorno

```text
DB_URL=jdbc:postgresql://localhost:5433/agroleak
DB_USERNAME=agroleak
DB_PASSWORD=agroleak
PORT=8081
CORS_ALLOWED_ORIGINS=*
DEMO_DATA_ENABLED=true
```

Para una entrega académica, `*` facilita conectar GitHub Pages/local frontend. Antes de producción se deben restringir los orígenes permitidos.

## Tests

```bash
mvn test
```

Incluye:

- `DetectionEngineTest`
  - fuga;
  - condición normal;
  - obstrucción;
  - presión fuera de rango.
- `ValveServiceTest`
  - evita comandos pendientes duplicados.
- `ReadingControllerIntegrationTest`
  - POST de telemetría + generación real de alerta en H2.

## Estructura principal

```text
src/main/java/pe/edu/upc/agroleak/
├── alerts/
├── analytics/
├── common/
├── demo/
├── devices/
├── irrigation/
├── monitoring/
├── pests/
└── AgroLeakApplication.java
```

## Roadmap

Ver [`docs/ROADMAP.md`](docs/ROADMAP.md).

Resumen:

```text
v0.1 REST + PostgreSQL + reglas + alertas + válvula manual
  ↓
v0.2 ESP32 real + MQTT + health/heartbeat
  ↓
v0.3 dashboard realtime + WebSocket/SSE
  ↓
v0.4 notificaciones
  ↓
v0.5 automatización segura
  ↓
v0.6 detección de anomalías con IA
  ↓
v0.7 visión artificial real
  ↓
v1.0 usuarios, fundos, sectores, seguridad y despliegue productivo
```

## Notas académicas

El objetivo de esta versión no es simular una plataforma industrial terminada. Se busca demostrar una arquitectura evolutiva y trazable que permita evidenciar:

- ingesta IoT;
- persistencia;
- procesamiento;
- respuesta ante eventos;
- integración REST;
- separación de responsabilidades;
- pruebas automatizadas;
- capacidad de crecimiento para los siguientes sprints.
