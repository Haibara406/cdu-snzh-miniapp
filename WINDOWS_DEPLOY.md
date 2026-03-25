# Windows Docker 部署指南

## 方案说明

使用 Docker Compose 在 Windows 本地部署，所有服务（包括应用）都运行在 Docker 容器中。

## 前置要求

1. **Docker Desktop for Windows**
   - 下载地址: https://www.docker.com/products/docker-desktop
   - 安装后启动 Docker Desktop
   - 确保 Docker 正在运行（系统托盘有 Docker 图标）

2. **Maven**（仅用于构建 jar 包）
   - 下载地址: https://maven.apache.org/download.cgi
   - 配置环境变量

## 快速开始

### 1. 配置环境变量

```cmd
# 复制配置文件
copy .env.example .env

# 编辑配置文件（用记事本打开）
notepad .env
```

### 2. 一键部署

```cmd
deploy.bat
```

脚本会自动：
1. 检查 Docker 是否运行
2. 构建应用 Docker 镜像
3. 启动所有服务（MySQL、Redis、MinIO、应用）
4. 显示访问地址

### 3. 访问应用

- 应用首页: http://localhost:8062
- API 文档: http://localhost:8062/doc.html
- 健康检查: http://localhost:8062/actuator/health
- MinIO 控制台: http://localhost:9001

## 常用命令

```cmd
# 首次部署
deploy.bat

# 查看服务状态
deploy.bat status

# 查看应用日志
deploy.bat logs app

# 查看所有日志
deploy.bat logs

# 停止所有服务
deploy.bat stop

# 启动所有服务
deploy.bat start

# 重启所有服务
deploy.bat restart

# 重新构建并更新应用
deploy.bat build

# 清理所有容器和数据
deploy.bat clean
```

## 或者直接使用 Docker Compose

```cmd
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f app

# 停止服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器和数据
docker-compose down -v
```

## 更新应用

当代码修改后，重新部署：

```cmd
# 方式一：使用脚本（推荐）
deploy.bat build

# 方式二：手动操作
cd cdu-snzh
mvn clean package -DskipTests
cd ..
docker build -t haibaraiii/cdu-snzh:latest cdu-snzh
docker-compose up -d --no-deps app
```

## 数据持久化

数据存储在 Docker volumes 中：

- `mysql-data`: MySQL 数据库
- `redis-data`: Redis 数据
- `minio-data`: MinIO 对象存储
- `./logs`: 应用日志（映射到宿主机）

即使删除容器，数据也会保留。使用 `deploy.bat clean` 可以完全清理。

## 故障排查

### 1. Docker 未启动

```
错误: Docker 未安装或未启动
解决: 启动 Docker Desktop
```

### 2. 端口被占用

编辑 `.env` 文件，修改端口：

```
MYSQL_PORT=3307
REDIS_PORT=6380
APP_PORT=8063
```

### 3. 容器启动失败

```cmd
# 查看具体错误
docker-compose logs app

# 查看所有容器状态
docker-compose ps
```

### 4. 数据库连接失败

```cmd
# 检查 MySQL 是否就绪
docker-compose logs mysql

# 重启 MySQL
docker-compose restart mysql

# 等待 30 秒后重启应用
docker-compose restart app
```

### 5. 镜像构建失败

```cmd
# 清理 Maven 缓存
cd cdu-snzh
mvn clean
cd ..

# 重新构建
deploy.bat build
```

## 网络架构

```
Windows 宿主机
  ↓
Docker Desktop
  ↓
Docker 网络 (cdu-snzh-network)
  ├─ cdu-snzh-backend (应用容器)
  ├─ cdu-snzh-mysql (MySQL 容器)
  ├─ cdu-snzh-redis (Redis 容器)
  └─ cdu-snzh-minio (MinIO 容器)
```

容器间通过服务名通信：
- 应用连接 MySQL: `mysql:3306`
- 应用连接 Redis: `redis:6379`
- 应用连接 MinIO: `minio:9000`

## 与远程部署的区别

| 特性 | Windows 本地部署 | 远程服务器部署 |
|------|------------------|----------------|
| 部署工具 | Docker Compose | deploy.sh |
| 配置文件 | .env | deploy.env |
| 镜像传输 | 本地构建 | SSH 传输 |
| 适用场景 | 开发/测试 | 生产环境 |

## 注意事项

1. **首次启动较慢**：需要下载 MySQL、Redis、MinIO 镜像（约 1GB）
2. **MySQL 初始化**：首次启动需要等待 30 秒初始化数据库
3. **健康检查**：应用会等待所有依赖服务就绪后才启动
4. **日志文件**：应用日志保存在 `./logs` 目录
5. **配置文件**：`.env` 包含敏感信息，不要提交到 Git

## 卸载

完全清理所有资源：

```cmd
# 停止并删除所有容器和数据
deploy.bat clean

# 或手动清理
docker-compose down -v
docker rmi haibaraiii/cdu-snzh:latest
```

## 推荐工作流

### 开发阶段

```cmd
# 1. 启动中间件
docker-compose up -d mysql redis minio

# 2. 在 IDE 中运行应用（方便调试）
# 配置 application-local.yml 连接 localhost

# 3. 停止中间件
docker-compose stop
```

### 测试阶段

```cmd
# 完整部署测试
deploy.bat

# 查看日志
deploy.bat logs app
```

### 生产部署

使用 `deploy.sh` 部署到远程服务器。
