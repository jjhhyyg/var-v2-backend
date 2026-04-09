# VAR 熔池视频分析系统 - 后端服务

简体中文 | [English](README.md)

> 负责任务管理、持久化、MQ 投递、WebSocket 推送和视频流服务的 Spring Boot 后端。

## 模块职责

后端是整个系统的调度中心，负责：

- 接收视频上传并创建分析任务
- 保存任务元数据和视频路径
- 向 RabbitMQ 投递分析消息
- 接收 AI 模块的进度和结果回调
- 将实时任务状态缓存到 Redis
- 通过 WebSocket 向前端推送状态和详情更新
- 提供原视频、预处理视频、结果视频的流式访问

## 技术栈

- Java 21
- Spring Boot 3.5.6
- Maven Wrapper
- PostgreSQL + Flyway
- Redis
- RabbitMQ
- Spring WebSocket
- SpringDoc OpenAPI

## 当前真实配置口径

不要再沿用旧 README 里的过时环境变量名。当前后端从 `backend/.env` 读取配置，核心变量包括：

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

应从主仓库根目录生成 `backend/.env`：

```bash
./scripts/use-env.sh dev
```

## 本地开发

### 推荐方式

开发阶段建议用 Docker 启中间件，后端本地直跑：

```bash
docker compose -f docker-compose.dev.yml up -d
```

然后进入 `backend` 目录启动：

```bash
./mvnw spring-boot:run
```

默认本地地址：

- `http://localhost:8080`

### IntelliJ IDEA

本地调试推荐直接使用 IntelliJ IDEA：

1. 打开 `backend` 目录
2. 配置 JDK 21
3. 等待 IDEA 导入 Maven 项目
4. 确认 `backend/.env` 已由主仓库脚本生成
5. 运行 `BackendApplication`
6. 观察 Flyway、Redis、RabbitMQ、WebSocket 的启动日志

适合用 IDEA 的场景：

- 需要打断点
- 需要查看启动阶段问题
- 需要观察数据库迁移、MQ、WebSocket 日志

如果只是快速跑起来，命令行方式也可以：

```bash
./mvnw spring-boot:run
```

## 健康检查与文档

- 健康检查：`http://localhost:8080/actuator/health`
- Swagger：`http://localhost:8080/swagger-ui.html`

## 关键接口区域

主要入口：

- 任务管理：`/api/tasks/*`
- AI 回调：`/api/tasks/{taskId}/progress`、`/result`、`/result-video`、`/preprocessed-video`、`/model-version`
- 视频流：`/api/videos/{taskId}/{type}`
- WebSocket 入口：`/ws`

关键订阅主题：

- `/topic/tasks/{taskId}/status`
- `/topic/tasks/{taskId}/update`
- `/topic/tasks/updates`

## 测试

执行后端自动化测试：

```bash
./mvnw test
```

但要清醒一点：现有自动化测试覆盖有限，不能替代本地联调测试。

## 构建与 Docker

### 本地构建 JAR

```bash
./mvnw clean package -DskipTests
```

### Docker 说明

- `Dockerfile`：默认生产 Dockerfile，要求 `target/` 中已有构建好的 JAR
- `Dockerfile.build`：在 Docker 内完成构建

如果你使用主仓库里的默认生产部署方式，通常需要先本地构建 JAR。

更多细节见 [`DOCKER.md`](DOCKER.md)。

## 下一步阅读

- 主仓库地址：
  `https://github.com/jjhhyyg/VAR-melting-defect-detection-source-code.git`
- 主仓库中的交接文档：
  `docs/项目接手、开发测试与部署指南.md`
