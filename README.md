[![CI Pipeline - Banco Digital Backend](https://github.com/FabricaEscuela2026-EAP13/banco-digital-backend/actions/workflows/ci.yml/badge.svg)](https://github.com/FabricaEscuela2026-EAP13/banco-digital-backend/actions/workflows/ci.yml)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=FabricaEscuela2026-EAP13_banco-digital-backend&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=FabricaEscuela2026-EAP13_banco-digital-backend)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=FabricaEscuela2026-EAP13_banco-digital-backend&metric=bugs)](https://sonarcloud.io/summary/new_code?id=FabricaEscuela2026-EAP13_banco-digital-backend)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=FabricaEscuela2026-EAP13_banco-digital-backend&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=FabricaEscuela2026-EAP13_banco-digital-backend)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=FabricaEscuela2026-EAP13_banco-digital-backend&metric=coverage)](https://sonarcloud.io/summary/new_code?id=FabricaEscuela2026-EAP13_banco-digital-backend)

# Banco Digital Backend

Backend REST para el proyecto Banco Digital de CodeFactory UdeA. Esta construido con Spring Boot, PostgreSQL, Flyway y seguridad JWT stateless.

## Estado actual

El repositorio incluye:

- Registro, login y autenticacion JWT.
- Actualizacion de datos personales del cliente autenticado.
- Creacion, consulta y saldo de cuentas bancarias.
- Transferencias entre cuentas.
- Historial y detalle de transacciones propias.
- Reportes de certificado bancario y movimientos por correo en PDF/CSV.
- Endpoints administrativos para clientes, cuentas y auditoria de transacciones.
- Manejo global de errores con formato estandar.
- Auditoria JPA (`created_at`, `updated_at`).
- Migraciones Flyway hasta `V13`.
- Swagger/OpenAPI con soporte para Bearer token.
- Actuator health y metricas Prometheus.
- Dockerfile y manifiestos base para Kubernetes.

## Stack tecnico

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC, Spring Data JPA, Spring Security, Spring HATEOAS
- JWT con `jjwt`
- PostgreSQL, Flyway y HikariCP
- Springdoc OpenAPI 3.0.2
- Actuator + Prometheus
- Spring Mail
- iText PDF y Apache Commons CSV para reportes
- JUnit, Mockito, H2, Cucumber, JaCoCo y SonarCloud
- Maven Wrapper

## Estructura principal

```text
src/main/java/co/edu/udea/bancodigital
  config/
  controllers/
  dtos/
  exception/
  models/
  repositories/
  services/

src/main/resources
  application.properties
  db/migration/

src/test
  java/
  resources/acceptance/

k8s/
```

## Requisitos previos

- JDK 21
- Maven, o el Maven Wrapper incluido (`mvnw` / `mvnw.cmd`)
- PostgreSQL accesible, si no se usan los valores de desarrollo configurados

## Configuracion

La aplicacion lee la configuracion principal desde `src/main/resources/application.properties`. Para ejecucion local existen valores por defecto de desarrollo, pero en despliegues reales se deben inyectar secretos por variables de entorno.

Variables principales:

| Variable | Uso |
|----------|-----|
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL |
| `DATASOURCE_USERNAME` | Usuario de la base de datos |
| `DATASOURCE_PASSWORD` | Contrasena de la base de datos |
| `APP_JWT_SECRET` | Secreto para firmar JWT |
| `APP_JWT_EXPIRATION` | Duracion del token en milisegundos, por defecto `86400000` |
| `SPRING_MAIL_HOST` | Host SMTP |
| `SPRING_MAIL_PORT` | Puerto SMTP |
| `SPRING_MAIL_USERNAME` | Usuario SMTP |
| `SPRING_MAIL_PASSWORD` | Contrasena SMTP |

Ejemplo en PowerShell:

```powershell
$env:APP_JWT_SECRET="TU_SECRETO_JWT"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://host:5432/db"
$env:DATASOURCE_USERNAME="usuario"
$env:DATASOURCE_PASSWORD="contrasena"
.\mvnw.cmd spring-boot:run
```

Ejemplo en Linux/macOS:

```bash
export APP_JWT_SECRET="TU_SECRETO_JWT"
export SPRING_DATASOURCE_URL="jdbc:postgresql://host:5432/db"
export DATASOURCE_USERNAME="usuario"
export DATASOURCE_PASSWORD="contrasena"
./mvnw spring-boot:run
```

La API queda disponible en:

- Base URL: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Health check: `http://localhost:8080/actuator/health`
- Prometheus: `http://localhost:8080/actuator/prometheus`

## Ejecutar pruebas y build

```bash
# Ejecutar pruebas
./mvnw test

# Build completo con reportes y verificaciones
./mvnw verify

# Compilar sin pruebas
./mvnw -DskipTests compile
```

En Windows reemplaza `./mvnw` por `.\mvnw.cmd`.

El proyecto contiene pruebas unitarias, de integracion y de aceptacion con Cucumber. El perfil `test` usa H2 en memoria y deshabilita Flyway.

## Migraciones con Flyway

Flyway esta habilitado y se ejecuta en el arranque de la aplicacion.

- Carpeta de migraciones: `src/main/resources/db/migration`
- Ultima migracion actual: `V13__seed_transferencia_tipo_transaccion.sql`
- Tabla de control: `flyway_schema_history`

Las migraciones aplicadas no deben editarse porque Flyway valida checksums. Para una base completamente vacia, valida primero que el esquema base requerido exista o agrega una migracion baseline consolidada.

Consulta util:

```sql
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Seguridad

Endpoints publicos:

- `POST /api/v1/auth/login`
- `POST /api/v1/usuarios/registro`
- Swagger/OpenAPI
- `GET /actuator/health`
- `GET /actuator/prometheus`

Los demas endpoints requieren:

```http
Authorization: Bearer <token>
```

Los endpoints bajo `/api/v1/admin/**` requieren autenticacion y validan rol `ADMIN` con `@PreAuthorize`.

## Endpoints disponibles

| Metodo | Ruta | Acceso | Descripcion |
|--------|------|--------|-------------|
| POST | `/api/v1/auth/login` | Publico | Iniciar sesion y obtener JWT |
| POST | `/api/v1/usuarios/registro` | Publico | Registrar cliente |
| PUT | `/api/v1/usuarios/me` | Autenticado | Actualizar datos personales |
| POST | `/api/v1/cuentas` | Autenticado | Crear cuenta bancaria |
| GET | `/api/v1/cuentas/me` | Autenticado | Listar mis cuentas |
| GET | `/api/v1/cuentas/{idCuenta}/saldo` | Autenticado | Consultar saldo |
| POST | `/api/v1/transferencias` | Autenticado | Realizar transferencia |
| GET | `/api/v1/transacciones/me` | Autenticado | Consultar historial con filtros y paginacion |
| GET | `/api/v1/transacciones/{idTransaccion}` | Autenticado | Consultar detalle de transaccion |
| POST | `/api/v1/reportes/certificado/{idCuenta}` | Autenticado | Solicitar certificado bancario por correo |
| POST | `/api/v1/reportes/movimientos/{idCuenta}` | Autenticado | Generar reporte de movimientos por rango de fechas |
| GET | `/api/v1/admin/clientes` | ADMIN | Listar clientes |
| GET | `/api/v1/admin/cuentas` | ADMIN | Listar cuentas del sistema |
| GET | `/api/v1/admin/transacciones` | ADMIN | Consultar auditoria de transacciones |

## Ejemplos de uso

### Registro

`POST /api/v1/usuarios/registro`

```json
{
  "idTipoDoc": 1,
  "numeroDocumento": "1032456789",
  "nombre": "Camilo",
  "primerApellido": "Mosquera",
  "segundoApellido": "Lopez",
  "direccion": "Calle 10 #20-30",
  "telefono": "3001234567",
  "correo": "camilo@example.com",
  "contrasena": "ClaveSegura1!"
}
```

### Login

`POST /api/v1/auth/login`

```json
{
  "correo": "camilo@example.com",
  "contrasena": "ClaveSegura1!"
}
```

Respuesta:

```json
{
  "token": "<jwt>",
  "tipo": "Bearer",
  "nombre": "Camilo",
  "correo": "camilo@example.com",
  "idRol": 2,
  "rol": "CLIENTE"
}
```

### Actualizar mis datos

`PUT /api/v1/usuarios/me`

```json
{
  "nombre": "Camilo Andres",
  "primerApellido": "Mosquera",
  "segundoApellido": "Lopez",
  "direccion": "Cra 45 #50-20",
  "telefono": "3009876543",
  "correo": "camilo.andres@example.com"
}
```

## Formato de errores

Las respuestas de error siguen un formato estandar:

```json
{
  "errorCode": "VALIDATION_ERROR",
  "message": "Error de validacion en la solicitud",
  "details": "telefono: El telefono debe ser celular colombiano: 10 digitos iniciando en 3",
  "traceId": "uuid",
  "timestamp": "2026-04-05T13:20:00"
}
```

## Docker

Construir imagen:

```bash
docker build -t bancodigital:v2 .
```

Ejecutar contenedor:

```bash
docker run --rm -p 8080:8080 \
  -e APP_JWT_SECRET="TU_SECRETO_JWT" \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host:5432/db" \
  -e DATASOURCE_USERNAME="usuario" \
  -e DATASOURCE_PASSWORD="contrasena" \
  bancodigital:v2
```

## Kubernetes

La carpeta `k8s/` incluye manifiestos para:

- `deployment.yaml`
- `service.yaml`
- `servicemonitor.yaml`
- `prometheus-values.yaml`

El `Deployment` espera un Secret llamado `bancodigital-secrets` con las variables de base de datos, JWT y correo.

## CI/CD

El workflow `.github/workflows/ci.yml` se ejecuta en push y pull request hacia `main`. El pipeline usa Java 21 Temurin, ejecuta:

```bash
mvn clean verify sonar:sonar -Dspring.profiles.active=test
```

Tambien publica cobertura JaCoCo hacia SonarCloud.

## Mi Contribución

Como desarrollador backend, contribuí al proyecto de manera integral en los siguientes aspectos:

### Desarrollo de Funcionalidades (HUs)

Implementé múltiples historias de usuario que forman la base del sistema bancario:

- **Autenticación y autorización**: Desarrollo de endpoints de login y registro de clientes con validación completa de datos.
- **Gestión de cuentas**: Implementación de servicios para creación, consulta de saldo y listado de cuentas bancarias.
- **Transferencias**: Lógica completa de transferencias entre cuentas con validaciones de saldo y seguridad.
- **Historial de transacciones**: Sistema de consulta de transacciones con filtros, paginación y detalle.
- **Reportes**: Generación de certificados bancarios y reportes de movimientos en PDF/CSV por correo.
- **Panel administrativo**: Endpoints protegidos para administración de clientes, cuentas y auditoría de operaciones.

### Seguridad y Autenticación (JWT + Spring Security)

Configuré e implementé el sistema de seguridad stateless basado en JWT:

- Implementación de autenticación con Bearer tokens utilizando la librería `jjwt`.
- Integración con Spring Security para validar y procesar tokens en cada solicitud.
- Control de acceso basado en roles (`@PreAuthorize`) para endpoints administrativos.
- Configuración de endpoints públicos vs protegidos con una estrategia de seguridad clara.
- Manejo seguro de secretos JWT mediante variables de entorno.

### Pruebas Unitarias

Desarrollé un conjunto robusto de pruebas unitarias usando JUnit, Mockito y H2:

- Cobertura de pruebas del **61.5%** a nivel general del proyecto (aportando aproximadamente el **49%** de esa cobertura).
- Pruebas de servicios, validaciones de DTOs, y lógica de negocio crítica.
- Pruebas de integración que validan endpoints completos.
- Pruebas de aceptación con Cucumber para escenarios de usuario.
- Uso del perfil `test` con base de datos H2 en memoria para aislamiento.

### Manejo de Excepciones

Diseñé e implementé un sistema global y consistente de manejo de errores:

- Creación de excepciones personalizadas para diferentes tipos de fallos (validación, autenticación, recursos no encontrados).
- Configuración de handlers globales que devuelven respuestas estructuradas con códigos de error, mensajes descriptivos y trazabilidad.
- Formato estandarizado en todas las respuestas de error con `errorCode`, `message`, `details`, `traceId` y `timestamp`.

### Infraestructura y DevOps

Contribuí a la configuración de infraestructura y automatización:

- Configuración del `Dockerfile` para contenerizar la aplicación con multi-stage builds optimizados.
- Implementación del pipeline CI/CD en `.github/workflows/ci.yml` que automatiza build, pruebas, análisis de código y cobertura.

### Optimización y Limpieza de Código

Realicé mejoras continuas en la calidad y eficiencia del código:

- Refactorización de servicios para mejorar legibilidad y reducir duplicación.
- Optimización de consultas a base de datos y lazy loading con JPA.
- Limpieza y estandarización de código siguiendo convenciones de Java.
- Configuración de Flyway para migraciones de base de datos consistentes (V1 a V13).
- Documentación con Swagger/OpenAPI para claridad en la API.
