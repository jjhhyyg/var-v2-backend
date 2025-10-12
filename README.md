# VAR Molten Pool Analysis System - Backend

[简体中文](README.zh.md) | English

> Spring Boot backend service for VAR molten pool video analysis system

## Tech Stack

- **Framework**: Spring Boot 3.5.6
- **Language**: Java 21
- **Database**: PostgreSQL + Flyway (migrations)
- **Cache**: Redis
- **Message Queue**: RabbitMQ
- **API Documentation**: SpringDoc OpenAPI (Swagger)
- **Video Processing**: JavaCV
- **Real-time Communication**: WebSocket

## Features

- RESTful API for video analysis task management
- Asynchronous task processing with RabbitMQ
- Real-time progress updates via WebSocket
- Video file upload and storage management
- Database schema versioning with Flyway
- Redis caching for improved performance
- Interactive API documentation with Swagger UI

## Prerequisites

- Java 21+
- PostgreSQL 13+
- Redis 6+
- RabbitMQ 3.9+
- Maven 3.8+ (or use included Maven Wrapper)

## Quick Start

### 1. Configure Environment Variables

Create a `.env` file in the backend directory (or use the parent project's environment configuration):

```bash
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/var_analysis
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest

# AI Processor
AI_PROCESSOR_URL=http://localhost:5000

# Storage
UPLOAD_DIR=/path/to/upload/directory
```

### 2. Start Infrastructure Services

If using Docker:

```bash
# From the project root
docker-compose -f docker-compose.dev.yml up -d
```

### 3. Run the Application

Using Maven Wrapper (recommended):

```bash
./mvnw spring-boot:run
```

Using system Maven:

```bash
mvn spring-boot:run
```

The application will start at http://localhost:8080

### 4. Access API Documentation

Visit http://localhost:8080/swagger-ui.html to explore the interactive API documentation.

## Development

### Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ustb/hyy/app/backend/
│   │   │       ├── config/          # Configuration classes
│   │   │       ├── controller/      # REST controllers
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access layer
│   │   │       ├── model/           # Entity models
│   │   │       ├── dto/             # Data transfer objects
│   │   │       └── exception/       # Exception handling
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway migration scripts
│   └── test/                        # Unit and integration tests
├── pom.xml
└── mvnw                             # Maven Wrapper
```

### Database Migrations

This project uses Flyway for database schema versioning. Migration scripts are located in `src/main/resources/db/migration/`.

To create a new migration:

```bash
# Create a new SQL file following the naming convention:
# V{version}__{description}.sql
# Example: V2__add_user_table.sql
```

### Running Tests

```bash
./mvnw test
```

### Building for Production

```bash
./mvnw clean package
```

The JAR file will be created in the `target/` directory.

## API Endpoints

### Task Management

- `POST /api/tasks` - Create a new analysis task
- `GET /api/tasks` - List all tasks
- `GET /api/tasks/{id}` - Get task details
- `DELETE /api/tasks/{id}` - Delete a task

### File Management

- `POST /api/files/upload` - Upload video file
- `GET /api/files/{filename}` - Download file

### WebSocket

- `/ws/progress` - Real-time task progress updates

For detailed API documentation, visit the Swagger UI at `/swagger-ui.html`.

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true

# File Upload
spring.servlet.multipart.max-file-size=2GB
spring.servlet.multipart.max-request-size=2GB
```

## Docker Deployment

Build Docker image:

```bash
docker build -t var-backend:latest .
```

Run with Docker:

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/var_analysis \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  var-backend:latest
```

## Troubleshooting

### Database Connection Issues

Check PostgreSQL is running and credentials are correct:

```bash
psql -h localhost -U postgres -d var_analysis
```

### RabbitMQ Connection Issues

Verify RabbitMQ is running:

```bash
# Check RabbitMQ status
docker ps | grep rabbitmq

# Check RabbitMQ management UI
open http://localhost:15672
```

## License

This project is licensed under the GNU Affero General Public License v3.0 (AGPL-3.0) - see the [LICENSE](LICENSE) file for details.

**Important:** Any modified version of this software used over a network must make the source code available to users.
