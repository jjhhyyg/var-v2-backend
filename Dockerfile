# 多阶段构建 - 后端Dockerfile

# 阶段1：构建阶段
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 复制pom.xml并下载依赖（利用Docker缓存层）
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# 阶段2：运行阶段
FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="侯阳洋"
LABEL description="VAR熔池视频分析系统 - 后端服务"

WORKDIR /app

# 安装必要工具
RUN apk add --no-cache curl

# 创建非root用户
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 从构建阶段复制jar文件
COPY --from=builder /app/target/*.jar app.jar

# 创建存储目录
RUN mkdir -p /var/var-analysis/storage/videos \
    /var/var-analysis/storage/results \
    /var/var-analysis/storage/temp && \
    chown -R appuser:appgroup /var/var-analysis

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
