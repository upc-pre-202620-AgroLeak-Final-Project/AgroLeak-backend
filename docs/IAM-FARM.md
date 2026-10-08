# IAM, Farm Management y Device Ownership

## Autenticación

`POST /api/v1/iam/auth/register` recibe `firstName`, `lastName`, `email` y `password` (8–72 caracteres, máximo 72 bytes UTF-8 por BCrypt). Crea un usuario activo con rol `FARMER`; normaliza el email a minúsculas y almacena solamente el hash BCrypt. Las respuestas usan DTOs sin contraseña ni hash. El registro público no acepta asignación de roles.

`POST /api/v1/iam/auth/login` recibe `email` y `password`. Devuelve `accessToken`, `tokenType: Bearer` y `user`. `GET /api/v1/iam/users/me` devuelve al usuario autenticado.

Enviar `Authorization: Bearer <accessToken>` en las peticiones protegidas. En Swagger (`/swagger-ui.html`), usar **Authorize** e introducir solamente el token. Registro, login, Swagger y `/actuator/health` son públicos. Los escenarios demo requieren autenticación y respetan ownership.

Los JWT se firman con HS256 e incluyen `sub` (UUID del usuario), `email`, `role`, `iss`, `iat` y `exp`. La infraestructura usa `NimbusJwtEncoder`, `NimbusJwtDecoder` y el filtro Bearer estándar de Spring Security, sin implementar un parser o filtro criptográfico propio. En cada petición se comprueba que el usuario siga activo y se obtiene su rol actual de persistencia.

- `JWT_SECRET`: secreto de al menos 32 bytes UTF-8. Puede generarse con `openssl rand -base64 32` y configurarse como variable de entorno; no guardarlo en Git.
- Sin `JWT_SECRET`, se genera una clave criptográfica aleatoria en memoria por arranque. Es útil para desarrollo local; los tokens anteriores dejan de servir al reiniciar. Para varias instancias o sesiones persistentes, configurar la misma clave externa.
- `JWT_EXPIRATION_MS`: por defecto `3600000` (una hora), mínimo `1000`.
- `CORS_ALLOWED_ORIGINS`: por defecto `http://localhost:4200,http://localhost:5173`.

## Roles y propiedad

| Rol | Acceso |
| --- | --- |
| FARMER | CRUD de sus Farms, Fields, Sectors y Crops; dispositivos propios o de sus sectores; lecturas y operaciones existentes sobre esos dispositivos. |
| ADMIN | Acceso global; puede asignar dispositivos antiguos sin propietario a sectores. |
| TECHNICIAN | Consulta global de dispositivos y monitoring; sin operaciones de escritura ni acceso global a Farms. |

Los roles ADMIN y TECHNICIAN se aprovisionan administrativamente en la base de datos; no se incluye un endpoint público para promover usuarios. Para una demo local, registrar una cuenta y actualizar su columna `users.role` a `ADMIN` mediante una herramienta de administración de PostgreSQL.

## Endpoints Farm

Todas las rutas tienen prefijo `/api/v1`. Las actualizaciones son `PUT` del conjunto de campos editables, sin modificar el padre ni el propietario.

| Recurso | Crear / listar | Consultar / actualizar / eliminar |
| --- | --- | --- |
| Farm | `POST /farms`, `GET /farms` | `GET`, `PUT`, `DELETE /farms/{id}` |
| Field | `POST`, `GET /farms/{farmId}/fields` | `GET`, `PUT`, `DELETE /fields/{id}` |
| Sector | `POST`, `GET /fields/{fieldId}/sectors` | `GET`, `PUT`, `DELETE /sectors/{id}` |
| Crop | `POST`, `GET /sectors/{sectorId}/crops` | `GET`, `PUT`, `DELETE /crops/{id}` |

Ejemplos de payloads para crear/actualizar:

```json
{"name":"Fundo Norte","location":"Ica","areaHectares":10}
```

```json
{"name":"Parcela A","areaHectares":5,"description":"Riego por goteo"}
```

```json
{"name":"Sector 1","areaHectares":2,"status":"ACTIVE"}
```

```json
{"name":"Tomate","variety":"Cherry","plantedAt":"2026-01-01","status":"PLANTED"}
```

Las áreas deben ser positivas (máximo 10 enteros y 4 decimales). `Sector.status`: `ACTIVE`, `INACTIVE`, `MAINTENANCE`. `Crop.status`: `PLANTED`, `GROWING`, `HARVESTED`, `INACTIVE`. Las fechas de plantación e instalación no pueden ser futuras.

`Farm.ownerId` se obtiene del usuario autenticado. Las referencias `Field.farmId`, `Sector.fieldId` y `Crop.sectorId` se validan al crear. No se elimina un Farm con Fields, un Field con Sectors ni un Sector con Crops o Devices (409); eliminar primero sus hijos o reasignar los dispositivos. No hay borrados en cascada.

## Devices y compatibilidad

Se conservan `POST /api/v1/devices`, `GET /api/v1/devices` y `GET /api/v1/devices/{id}`. El payload antiguo con `name` y `location` sigue siendo válido.

La creación admite opcionalmente `deviceType`, `status`, `batteryLevel` (0–100), `firmwareVersion`, `installationDate` y `sectorId`. `lastSeen` se sigue actualizando al recibir lecturas. Tipos: `GATEWAY`, `FLOW_SENSOR`, `PRESSURE_SENSOR`, `SOIL_MOISTURE_SENSOR`, `CAMERA`, `VALVE`. Estados: `ONLINE`, `OFFLINE`, `MAINTENANCE`; predeterminado `OFFLINE`.

```http
PATCH /api/v1/devices/{id}/sector
Content-Type: application/json
Authorization: Bearer <token>

{"sectorId":"UUID-del-sector"}
```

Se valida tanto acceso al dispositivo como ownership del sector destino. Los dispositivos nuevos sin sector conservan un `ownerId` interno para que otro FARMER no pueda reclamarlos. En dispositivos asociados, la propiedad se determina por el sector. Las columnas nuevas son nullable para conservar datos anteriores; los dispositivos históricos sin propietario son visibles para ADMIN/TECHNICIAN y únicamente ADMIN puede asignarlos inicialmente.

## DDD y persistencia

`iam` y `farm` separan aplicación, dominio, contratos de repositorio, adaptadores JPA y REST/DTO. `CurrentUser` y `TokenIssuer` son puertos de aplicación IAM. `FarmAccessService` resuelve propiedad; `SectorDeviceUsage` permite comprobar dependencias al eliminar un sector sin depender de entidades Devices. Su implementación pertenece a Devices.

Las nuevas asociaciones entre contextos usan UUID (`Farm.ownerId`, `Device.sectorId`, `Device.ownerId`), sin relaciones JPA bidireccionales entre sus aggregates. Se conservan las relaciones históricas de monitoring/alerts/irrigation/pests con Device. Se mantiene el esquema académico existente con Hibernate `ddl-auto: update`; no se introduce un sistema de migraciones de producción.

El handler global conserva respuestas uniformes: validación 400, credenciales/token 401, ownership 403, recurso inexistente 404 y conflictos 409. Las respuestas 401 del filtro incluyen `WWW-Authenticate: Bearer`.

## Validación

`mvn clean test` ejecuta pruebas JUnit/Mockito y MockMvc con H2. Se verifica registro y hash, duplicados, login, JWT válido/expirado/firma incorrecta, usuario inactivo, `/me`, roles, CORS, ownership de la jerarquía y dispositivos, CRUD, referencias inexistentes, eliminación con dependencias, payloads antiguos y Swagger. Se preservan las pruebas anteriores.

No se agregan refresh tokens, recuperación de contraseña, OAuth externo ni nuevas capacidades avanzadas de monitoring o analytics.
