# AgroLeak Backend

Backend académico para monitorear riego y plagas, detectar anomalías con reglas explicables y operar válvulas manualmente. Java 21, Spring Boot 3.5, Maven, PostgreSQL, JPA, Spring Security/JWT, Flyway, Swagger/OpenAPI y JUnit/Mockito.

## Implemented ~80-85%

El porcentaje expresa el alcance funcional de la entrega, no cobertura de tests ni una certificación de producción.

- IAM: registro, BCrypt, login JWT, roles ADMIN/FARMER/TECHNICIAN y `/users/me`.
- Farm Management: Farm, Field, Sector y Crop; propiedad por usuario.
- Devices: tipos, estado, batería, firmware, instalación, última actividad y sector.
- Monitoring: ingesta, históricos, últimas lecturas y resúmenes AVG/MIN/MAX.
- Reglas LEAK, OBSTRUCTION, LOW_PRESSURE, HIGH_PRESSURE y DEVICE_OFFLINE.
- Alerts: filtros, ACTIVE/ACKNOWLEDGED/RESOLVED, severidad y auditoría de acciones.
- Irrigation: OPEN/CLOSE manual, confirmación y modos de operación.
- Pests: observaciones por dispositivo o sector, imágenes por URL y alerta PEST_DETECTED.
- Analytics: indicadores de consumo estimado, pérdida, alertas, dispositivos y plagas; series para gráficas.
- Demo coherente, perfiles DEV/PROD, esquema versionado y conexión PostgreSQL cloud mediante variables.

## Arquitectura DDD

Modular monolith con bounded contexts `iam`, `farm`, `devices`, `monitoring`, `alerts`, `irrigation`, `pests` y `analytics`. `demo` es soporte académico y `common` contiene configuración, rangos temporales y errores compartidos.

Cada módulo separa `application`, `domain/model`, `infrastructure` y `presentation/rest`; IAM/Farm incluyen contratos `domain/repository` y adaptadores de persistencia. Las asociaciones nuevas entre contextos usan UUID. Se conservan las relaciones JPA históricas a Device para mantener compatibilidad. Analytics consulta otros módulos; la integración de caudales pertenece a Monitoring y las plagas publican un evento de aplicación que Alerts consume dentro de la misma transacción.

Detalles: [arquitectura](docs/ARCHITECTURE.md), [IAM y Farm](docs/IAM-FARM.md), [contratos y decisiones del bloque final](docs/MONITORING-PRODUCTION.md).

## Requisitos y Docker

- Java 21, Maven 3.9+.
- Docker Desktop para PostgreSQL local, o un servidor PostgreSQL compatible.

```bash
docker compose up -d
mvn spring-boot:run
```

DEV es el perfil predeterminado. También puede seleccionarse con `SPRING_PROFILES_ACTIVE=dev`. PostgreSQL local: `localhost:5433`, base/usuario/contraseña `agroleak`. Backend: `http://localhost:8081`.

- Swagger: http://localhost:8081/swagger-ui/index.html
- OpenAPI: http://localhost:8081/v3/api-docs
- Health: http://localhost:8081/actuator/health

Flyway ejecuta las migraciones antes de que Hibernate valide el esquema. No se utiliza `ddl-auto: update` en DEV ni PROD.

## Variables de entorno

| Variable | DEV / comportamiento |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` por defecto; usar `prod` en despliegue |
| `PORT` | `8081` |
| `DB_URL` | `jdbc:postgresql://localhost:5433/agroleak`; obligatorio en PROD |
| `DB_USERNAME`, `DB_PASSWORD` | `agroleak` en DEV; obligatorios en PROD |
| `JWT_SECRET` | Opcional en DEV; obligatorio en PROD, mínimo 32 bytes UTF-8 |
| `JWT_EXPIRATION_MS` | `3600000` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200,http://localhost:5173`; lista explícita obligatoria en PROD |
| `DEMO_DATA_ENABLED` | `true` en DEV; demo siempre excluido en PROD |
| `DEMO_EMAIL`, `DEMO_PASSWORD` | Cuenta de demostración local; ver siguiente sección |
| `DEVICE_OFFLINE_SECONDS` | `300`; revisión cada 60 segundos |
| `FLOW_MAX_HOLD_SECONDS` | `300`; antigüedad máxima del caudal para estimar litros |
| `PEST_ALERT_MIN_COUNT`, `PEST_ALERT_MIN_CONFIDENCE` | `5`, `0.7` |
| `LEAK_THRESHOLD_PERCENT` | `20.0` |
| `PRESSURE_MIN_BAR`, `PRESSURE_MAX_BAR` | `1.0`, `4.0` |

Sin `JWT_SECRET`, DEV genera una clave aleatoria por arranque; los tokens anteriores dejan de servir al reiniciar. Puede generarse una clave externa con `openssl rand -base64 32`. No guardar secretos reales en Git. `.env.example` contiene exclusivamente valores locales; Spring no carga un archivo `.env` automáticamente: exportar las variables o configurarlas en el IDE/proveedor.

## IAM y autorización

```http
POST /api/v1/iam/auth/register
POST /api/v1/iam/auth/login
GET  /api/v1/iam/users/me
```

Registro: `firstName`, `lastName`, `email`, `password`. Login: `email`, `password`. Usar `accessToken` en `Authorization: Bearer <token>` o introducir solamente el token en **Authorize** de Swagger.

FARMER accede a recursos propios. ADMIN tiene acceso global. TECHNICIAN puede consultar dispositivos/monitoring; no ejecutar escrituras. Los usuarios inactivos dejan de poder usar sus JWT. Las cuentas administrativas se aprovisionan explícitamente en la base de datos; el registro público siempre crea FARMER.

Registro/login, Swagger y health son públicos. Todos los demás endpoints, incluidos los de demo, requieren autenticación.

## Endpoints principales

Prefijo común `/api/v1`; las rutas históricas siguen disponibles.

| Módulo | Rutas |
| --- | --- |
| Farm | CRUD `/farms`, `/fields/{id}`, `/sectors/{id}`, `/crops/{id}`; creación/listado bajo sus padres |
| Devices | `POST/GET /devices`, `GET /devices/{id}`, `PATCH /devices/{id}/sector` |
| Monitoring | `POST/GET /monitoring/readings`; `GET /monitoring/devices/{deviceId}/latest`, `/latest-by-type`, `/summary` |
| Compatibilidad Monitoring | `POST /readings`, `GET /readings/device/{deviceId}`, `/readings/device/{deviceId}/latest` |
| Alerts | `GET /alerts`; `PATCH /alerts/{id}/acknowledge`, `/alerts/{id}/resolve` |
| Irrigation | `POST /valves/{deviceId}/commands`; `PATCH /valves/commands/{commandId}/confirm`; `GET /valves/{deviceId}/latest`; `GET/PUT /valves/{deviceId}/mode` |
| Pests | `POST/GET /pests/observations`; alias `/pest-observations`; `GET /pest-observations/device/{deviceId}` |
| Analytics | `GET /analytics/dashboard`, `GET /analytics/charts?deviceId=...`; dashboard anterior `/dashboard/{deviceId}` |
| Demo | `GET /demo`; `POST /demo/scenarios/normal/{deviceId}`, `/leak/{deviceId}`, `/obstruction/{deviceId}` |

Históricos: `deviceId`, `sensorType`, `from`, `to`, `limit`. Fechas ISO-8601, intervalos `[from,to)`, máximo 31 días, últimas 24 horas por defecto; `limit` entre 1 y 1000. Alertas: `deviceId`, `status`, `severity`, `type`, `limit`. Los filtros nunca amplían la propiedad del usuario autenticado.

## Cuenta maestra para la presentación

Al arrancar con el perfil `dev` (predeterminado) y `DEMO_DATA_ENABLED=true`,
se crea automáticamente esta cuenta adicional con acceso global **ADMIN**:

```json
{"email":"maestro@agroleak.local","password":"123456789"}
```

Iniciar sesión desde el app o con `POST /api/v1/iam/auth/login`.
La contraseña se almacena con BCrypt. Esta cuenta de presentación no se crea
con el perfil `prod` ni cuando se deshabilitan los datos demo.

Incluye la finca **AgroLeak Presentacion**, una parcela, dos sectores, dos
cultivos y seis dispositivos; siete días de lecturas cada cinco minutos
(más de 8000 registros), siete observaciones de plagas, una fuga resuelta,
una inspección reconocida, una alerta de plagas activa y tres ciclos OPEN/CLOSE
confirmados. Las lecturas y observaciones tienen fechas históricas; las acciones
y su auditoría se registran al preparar la demo. Todos son datos simulados.

Reiniciar conserva los datos y la contraseña, sin duplicarlos. Los siete días
se calculan respecto al primer arranque: para verlos posteriormente, seleccionar
ese intervalo en los históricos. No se reemplaza ni se eleva el rol de una cuenta
existente con el mismo correo. La cuenta demo anterior sigue disponible.

## Flujo demo

Solo en DEV se crea una cuenta **local académica**:

```json
{"email":"demo@agroleak.local","password":"AgroLeakDemo123!"}
```

Se almacena con BCrypt. Cambiar `DEMO_EMAIL`/`DEMO_PASSWORD` antes del primer arranque si se desean otros valores; reiniciar no reemplaza la contraseña de una cuenta existente. Esta contraseña pública no se utiliza en PROD.

El seed transaccional crea una farm, un field, dos sectors, un crop, gateway, sensores de caudal/presión/humedad, válvula y cámara. Incluye una hora de lecturas normales, una observación de plagas bajo el umbral y un comando CLOSE confirmado. Los reinicios no duplican el seed ni alteran dispositivos antiguos.

1. Login con el usuario demo y autorizar Swagger.
2. `GET /api/v1/demo`: obtener `scenarioDeviceId`, farms y dispositivos visibles.
3. Ejecutar NORMAL sobre `scenarioDeviceId`; revisar latest, summary y Analytics.
4. Ejecutar LEAK; filtrar `/alerts?type=LEAK`, reconocer y resolver la alerta.
5. Ejecutar OBSTRUCTION; comprobar OBSTRUCTION y la pérdida estimada.
6. Crear OPEN/CLOSE sobre el dispositivo VALVE; confirmar con `{"success":true}`.
7. Enviar una observación con `count >= 5` y `confidence >= 0.7`; comprobar PEST_DETECTED.
8. Volver a NORMAL y resolver los incidentes anteriores. Las alertas no se resuelven automáticamente al normalizar lecturas.

Cada escenario guarda una muestra completa antes de evaluarla, evitando anomalías transitorias por mezclar muestras de escenarios distintos. El gateway representa un único punto de medición; los sensores registrados no duplican sus caudales en el seed.

## Flyway y transición local

Migraciones en `src/main/resources/db/migration/`:

- V1: esquema previo IAM/Farm/IoT, con `CREATE TABLE IF NOT EXISTS`.
- V2: estados/tipos de alertas, auditoría, referencias de plagas y modos de riego.
- V3: integridad referencial de la jerarquía agrícola y Device → Sector.

DEV usa `baseline-on-migrate=true` con versión **0** exclusivamente para adoptar el esquema local previo de Hibernate. Ejecuta V1–V3 y conserva las filas. PROD no hace baseline automático. No modificar scripts ya aplicados: agregar otra versión.

Antes de adoptar una base existente, respaldarla y comprobar que corresponda al esquema AgroLeak anterior. Un esquema incompatible debe revisarse; no se borran filas para ocultar inconsistencias. Para adoptar explícitamente una base previa en PROD, usar Flyway CLI con `baseline -baselineVersion=0` sobre la conexión revisada y luego iniciar la aplicación.

Si se desea **reinicializar voluntariamente una base local descartable**, estos comandos eliminan el volumen PostgreSQL y todos sus datos:

```bash
docker compose down -v
docker compose up -d
mvn spring-boot:run
```

No son necesarios para la transición normal ni deben ejecutarse sobre datos que se quieran conservar.

## Preparación para cloud

Railway, Neon, Supabase PostgreSQL o Render PostgreSQL pueden proporcionar la conexión. El backend no depende de un proveedor ni crea una base cloud.

```text
SPRING_PROFILES_ACTIVE=prod
PORT=8081
DB_URL=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
DB_USERNAME=<usuario-del-proveedor>
DB_PASSWORD=<password-del-proveedor>
JWT_SECRET=<secreto-externo-de-al-menos-32-bytes>
JWT_EXPIRATION_MS=3600000
CORS_ALLOWED_ORIGINS=https://frontend.example.com
```

Usar una URL **JDBC**; una URL `postgres://usuario:password@host/db` no se acepta directamente. Separar usuario/contraseña en sus variables y conservar las opciones TLS requeridas por el proveedor. Flyway necesita permisos DDL para aplicar las migraciones. PROD usa `validate`, pool configurable con `DB_POOL_SIZE` (5 por defecto), sin demo y sin secretos de desarrollo.

```bash
mvn -DskipTests package
java -jar target/agroleak-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Tests y verificación

```bash
mvn clean test
mvn -DskipTests package
```

JUnit/Mockito y MockMvc/H2 cubren IAM/JWT, Farm/Device ownership, detección, rangos y estadísticas, alert acknowledge/resolve, riego, plagas, Analytics, cálculo de litros, offline y seed idempotente. H2 usa `create-drop` y desactiva Flyway porque los scripts de migración están dirigidos a PostgreSQL.

Para ejecutar también las pruebas reales de PostgreSQL local:

```bash
AGROLEAK_TEST_POSTGRES_URL=jdbc:postgresql://localhost:5433/agroleak mvn clean test
```

Opcionalmente configurar `AGROLEAK_TEST_POSTGRES_USERNAME` y `AGROLEAK_TEST_POSTGRES_PASSWORD` (por defecto los valores de Docker local). Las pruebas crean y eliminan únicamente esquemas temporales con nombres aleatorios; verifican esquema nuevo, JPA y adopción del esquema previo conservando registros. Sin esa variable, se omiten explícitamente esas dos pruebas. No se necesita Testcontainers ni acceso a Docker para la suite rápida.

## Pendiente / roadmap: 15-20% restante

- MQTT real e identidad propia de dispositivos para confirmar comandos.
- WebSockets/SSE.
- Computer Vision real y ML.
- Notificaciones push/WhatsApp.
- Automatización real AUTO_SAFE; hoy solo existe como configuración y bloquea comandos.
- GIS avanzado.
- Refresh tokens avanzados, password recovery y verificación de email.
- Calibración física de sensores y validación de las estimaciones de consumo.

No se incluyen microservicios, Kafka, RabbitMQ, Kubernetes ni Redis. El frontend Angular es el siguiente bloque de trabajo independiente.
