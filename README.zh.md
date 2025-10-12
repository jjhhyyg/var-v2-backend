# VAR 熔池视频分析系统 - 后端服务

简体中文 | [English](README.md)

> 基于 Spring Boot 的 VAR 熔池视频分析系统后端服务

## 技术栈

- **框架**: Spring Boot 3.5.6
- **语言**: Java 21
- **数据库**: PostgreSQL + Flyway（数据库迁移）
- **缓存**: Redis
- **消息队列**: RabbitMQ
- **API 文档**: SpringDoc OpenAPI (Swagger)
- **视频处理**: JavaCV
- **实时通信**: WebSocket

## 核心功能

- RESTful API 视频分析任务管理
- 基于 RabbitMQ 的异步任务处理
- 通过 WebSocket 实时推送进度更新
- 视频文件上传与存储管理
- Flyway 数据库版本控制
- Redis 缓存提升性能
- Swagger UI 交互式 API 文档

## 环境要求

- Java 21+
- PostgreSQL 13+
- Redis 6+
- RabbitMQ 3.9+
- Maven 3.8+（或使用内置的 Maven Wrapper）

## 快速开始

### 1. 配置环境变量

在 backend 目录下创建 `.env` 文件（或使用父项目的环境配置）：

```bash
# 数据库
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

# AI 处理模块
AI_PROCESSOR_URL=http://localhost:5000

# 存储
UPLOAD_DIR=/path/to/upload/directory
```

### 2. 启动基础设施服务

如果使用 Docker：

```bash
# 从项目根目录执行
docker-compose -f docker-compose.dev.yml up -d
```

### 3. 运行应用

使用 Maven Wrapper（推荐）：

```bash
./mvnw spring-boot:run
```

使用系统 Maven：

```bash
mvn spring-boot:run
```

应用将在 http://localhost:8080 启动

### 4. 访问 API 文档

访问 http://localhost:8080/swagger-ui.html 查看交互式 API 文档。

## 开发指南

### 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ustb/hyy/app/backend/
│   │   │       ├── config/          # 配置类
│   │   │       ├── controller/      # REST 控制器
│   │   │       ├── service/         # 业务逻辑
│   │   │       ├── repository/      # 数据访问层
│   │   │       ├── model/           # 实体模型
│   │   │       ├── dto/             # 数据传输对象
│   │   │       └── exception/       # 异常处理
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/        # Flyway 迁移脚本
│   └── test/                        # 单元测试和集成测试
├── pom.xml
└── mvnw                             # Maven Wrapper
```

### 数据库迁移

本项目使用 Flyway 进行数据库版本控制。迁移脚本位于 `src/main/resources/db/migration/`。

创建新的迁移脚本：

```bash
# 创建遵循命名规范的 SQL 文件：
# V{版本号}__{描述}.sql
# 例如：V2__add_user_table.sql
```

### 运行测试

```bash
./mvnw test
```

### 生产环境构建

```bash
./mvnw clean package
```

JAR 文件将生成在 `target/` 目录下。

## API 接口

### 任务管理

- `POST /api/tasks` - 创建新的分析任务
- `GET /api/tasks` - 获取所有任务列表
- `GET /api/tasks/{id}` - 获取任务详情
- `DELETE /api/tasks/{id}` - 删除任务

### 文件管理

- `POST /api/files/upload` - 上传视频文件
- `GET /api/files/{filename}` - 下载文件

### WebSocket

- `/ws/progress` - 实时任务进度更新

详细的 API 文档请访问 Swagger UI：`/swagger-ui.html`

## 配置说明

`application.properties` 中的主要配置项：

```properties
# 服务器
server.port=8080

# 数据库
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Flyway
spring.flyway.enabled=true

# 文件上传
spring.servlet.multipart.max-file-size=2GB
spring.servlet.multipart.max-request-size=2GB
```

## Docker 部署

构建 Docker 镜像：

```bash
docker build -t var-backend:latest .
```

使用 Docker 运行：

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/var_analysis \
  -e DATABASE_USERNAME=postgres \
  -e DATABASE_PASSWORD=password \
  var-backend:latest
```

## 故障排查

### 数据库连接问题

检查 PostgreSQL 是否运行且凭据正确：

```bash
psql -h localhost -U postgres -d var_analysis
```

### RabbitMQ 连接问题

验证 RabbitMQ 是否运行：

```bash
# 检查 RabbitMQ 状态
docker ps | grep rabbitmq

# 访问 RabbitMQ 管理界面
open http://localhost:15672
```

## 许可证

本项目采用 GNU Affero General Public License v3.0 (AGPL-3.0) 许可证 - 详见 [LICENSE](LICENSE) 文件。

**重要提示：** 任何通过网络使用的本软件修改版本必须向用户提供源代码。
