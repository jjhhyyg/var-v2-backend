# VAR Molten Pool Analysis System - Backend

[简体中文](README.zh.md) | English

> Spring Boot backend service for task management, persistence, MQ dispatch, WebSocket updates, and video streaming.

## Responsibilities

The backend is the orchestration center of the system. It is responsible for:

- receiving uploaded videos and creating tasks
- saving task metadata and file paths
- sending analysis messages to RabbitMQ
- receiving AI callbacks for progress and final results
- caching real-time task status in Redis
- pushing task updates to the frontend over WebSocket
- serving original, preprocessed, and result videos

## Tech Stack

- Java 21
- Spring Boot 3.5.6
- Maven Wrapper
- PostgreSQL + Flyway
- Redis
- RabbitMQ
- Spring WebSocket
- SpringDoc OpenAPI

## Configuration Source of Truth

Do not follow outdated environment variable names from old documents. The current backend reads configuration from `backend/.env` and uses these variables:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `REDIS_PASSWORD`
- `REDIS_DB`
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USER`
- `RABBITMQ_PASSWORD`
- `RABBITMQ_VHOST`
- `BACKEND_BASE_URL`
- `SERVER_PORT`
- `CORS_ORIGINS`
- `STORAGE_*`

Generate `backend/.env` from the main repository root:

```bash
./scripts/use-env.sh dev
```

## Local Development

### Recommended workflow

In local development, infrastructure should run in Docker while the backend runs locally:

```bash
docker compose -f docker-compose.dev.yml up -d
```

Then run the backend from the `backend` directory:

```bash
./mvnw spring-boot:run
```

Default local URL:

- `http://localhost:8080`

### IntelliJ IDEA

Recommended for local debugging:

1. Open the `backend` directory in IntelliJ IDEA
2. Configure JDK 21
3. Let IDEA import the Maven project
4. Make sure `backend/.env` already exists
5. Run `BackendApplication`
6. Watch the startup logs for Flyway, Redis, RabbitMQ, and WebSocket initialization

Use IDEA when you need breakpoints, startup diagnostics, or request tracing. Use `./mvnw spring-boot:run` when you only need a quick local run.

## Health Check and API Docs

- Health: `http://localhost:8080/actuator/health`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Important API Areas

Key backend entry points:

- Task management: `/api/tasks/*`
- AI callbacks: `/api/tasks/{taskId}/progress`, `/result`, `/result-video`, `/preprocessed-video`, `/model-version`
- Video streaming: `/api/videos/{taskId}/{type}`
- WebSocket endpoint: `/ws`

Important WebSocket topics:

- `/topic/tasks/{taskId}/status`
- `/topic/tasks/{taskId}/update`
- `/topic/tasks/updates`

## Tests

Run backend tests with:

```bash
./mvnw test
```

Be realistic: automated backend tests exist, but they are not sufficient to replace full local integration testing.

## Build and Docker

### Build a local JAR

```bash
./mvnw clean package -DskipTests
```

### Docker behavior

- `Dockerfile`: default production Dockerfile, expects a prebuilt JAR in `target/`
- `Dockerfile.build`: builds the JAR inside Docker

If you deploy from the main repository with the default backend Dockerfile, build the JAR first.

For more details, see [`DOCKER.md`](DOCKER.md).

## What to Read Next

- Main repository overview:
  `https://github.com/jjhhyyg/VAR-melting-defect-detection-source-code.git`
- Main handover guide in the root repository:
  `docs/项目接手、开发测试与部署指南.md`
