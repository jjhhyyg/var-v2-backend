# 多阶段构建 - 后端Dockerfile

# 阶段1：构建阶段
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app/backend

# 复制pom.xml并下载依赖（利用Docker缓存层）
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# 阶段2：运行阶段
# 使用 Debian 基础镜像，因为 JavaCV 的预编译库基于 glibc
# Alpine 使用 musl libc，与 JavaCV 不兼容
FROM eclipse-temurin:21-jre

LABEL maintainer="侯阳洋"
LABEL description="VAR熔池视频分析系统 - 后端服务"

WORKDIR /app/backend

# 安装必要工具
# JavaCV 已在 pom.xml 中配置为 javacv-platform，包含所有平台的 FFmpeg 预编译库（用于视频编码）
# 另外安装 ffmpeg 命令行工具（用于 faststart 优化，使视频支持浏览器流式播放）
# 使用 Debian 镜像确保与 JavaCV 的 glibc 依赖兼容
# 额外安装 ffmpeg 和 ffprobe CLI 工具用于视频转码
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# 创建非root用户（Debian 语法）
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# 从构建阶段复制jar文件
COPY --from=builder /app/backend/target/*.jar app.jar

# 创建存储目录
RUN mkdir -p ../storage/videos \
    ../storage/results \
    ../storage/temp && \
    chown -R appuser:appgroup /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM参数优化
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/var-analysis/heapdump.hprof"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
