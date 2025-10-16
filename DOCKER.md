# Backend Docker 构建说明

本目录包含两种 Dockerfile 构建方式，可根据不同场景选择使用。

## Dockerfile 文件说明

### 1. `Dockerfile` - 简化版（推荐用于生产部署）
- **特点**：直接复制本地编译好的 jar 包
- **优势**：
  - 构建速度快（跳过依赖下载和编译）
  - 镜像体积小（不包含构建工具）
  - 适合 CI/CD 流程
- **前置要求**：需要本地先编译 jar 包

### 2. `Dockerfile.build` - 多阶段构建版
- **特点**：在 Docker 内完成编译打包
- **优势**：
  - 不需要本地 Java 环境
  - 构建环境一致性好
- **劣势**：
  - 构建时间较长
  - 需要下载所有依赖

---

## 使用方法

### 方式一：使用简化版 Dockerfile（默认）

#### 步骤 1：本地编译 jar 包
```bash
cd backend
./mvnw clean package -DskipTests
```

#### 步骤 2：构建并运行
```bash
# 回到项目根目录
cd ..

# GPU 版本
docker compose -f docker-compose.prod.yml up --build -d

# CPU 版本
docker compose -f docker-compose.prod.cpu.yml up --build -d
```

---

### 方式二：使用多阶段构建 Dockerfile.build

**不需要本地编译**，直接在 Docker 内构建：

```bash
# GPU 版本
BACKEND_DOCKERFILE=Dockerfile.build docker compose -f docker-compose.prod.yml up --build -d

# CPU 版本
BACKEND_DOCKERFILE=Dockerfile.build docker compose -f docker-compose.prod.cpu.yml up --build -d
```

或者设置环境变量后再构建：

```bash
# Linux/Mac
export BACKEND_DOCKERFILE=Dockerfile.build

# Windows PowerShell
$env:BACKEND_DOCKERFILE="Dockerfile.build"

# 然后构建
docker compose -f docker-compose.prod.yml up --build -d
```

---

## 选择建议

| 场景 | 推荐方式 | 原因 |
|------|---------|------|
| **CI/CD 流程** | 简化版 Dockerfile | 速度快，适合频繁构建 |
| **生产部署（有 Java 环境）** | 简化版 Dockerfile | 构建快速，镜像小 |
| **生产部署（无 Java 环境）** | Dockerfile.build | 无需本地环境 |
| **初次部署/测试** | Dockerfile.build | 简单，无需配置 |

---

## 常见问题

### Q1: 使用简化版 Dockerfile 时提示找不到 jar 文件？
**A**: 需要先在本地编译：
```bash
cd backend
./mvnw clean package -DskipTests
```

### Q2: 如何查看当前使用的是哪个 Dockerfile？
**A**: 查看环境变量：
```bash
# Linux/Mac
echo $BACKEND_DOCKERFILE

# Windows PowerShell
echo $env:BACKEND_DOCKERFILE

# 如果输出为空或 "Dockerfile"，则使用简化版
# 如果输出为 "Dockerfile.build"，则使用多阶段构建版
```

### Q3: 两种方式构建的镜像有什么区别？
**A**: 最终运行的镜像完全一致，只是构建方式不同：
- 简化版：在本地编译后打包进镜像
- 多阶段构建：在 Docker 内编译后打包进镜像

---

## 技术细节

### 基础镜像
- **运行时镜像**：`eclipse-temurin:21-jre`（Debian 基础）
- **为什么不用 Alpine**：JavaCV 依赖 glibc，Alpine 使用 musl libc 不兼容

### 系统依赖
- `curl`：健康检查
- `ffmpeg`：视频编码和格式转换
  - JavaCV 提供跨平台 FFmpeg 预编译库
  - 额外安装 CLI 工具用于 faststart 优化（浏览器流式播放）

### 安全配置
- 使用非 root 用户运行（`appuser`）
- 最小化权限原则

---

## 相关文件
- `Dockerfile` - 简化版 Dockerfile
- `Dockerfile.build` - 多阶段构建版 Dockerfile
- `../../docker-compose.prod.yml` - 生产环境配置（GPU）
- `../../docker-compose.prod.cpu.yml` - 生产环境配置（CPU）
