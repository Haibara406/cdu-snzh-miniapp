# CDU-SNZH 智能部署脚本使用说明

## 功能特性

✅ 自动检测并安装 Docker
✅ 智能检测已存在的中间件（MySQL、Redis、MinIO）
✅ 自动部署缺失的中间件
✅ 使用 Docker 网络实现服务间通信（容器重启 IP 不变）
✅ 支持本地和远程部署
✅ 一键构建并部署应用
✅ 健康检查和状态监控
✅ 敏感配置通过环境变量管理

## 架构说明

脚本会创建一个名为 `cdu-snzh-network` 的 Docker 网络，所有容器都加入此网络。容器间通过容器名通信（如 `mysql`、`redis`、`minio`），无需关心 IP 地址变化。

## 快速开始

### 1. 准备工作

确保本地已安装：
- Docker（用于构建镜像）
- SSH 客户端（远程部署时需要）

配置 SSH 密钥认证（推荐）：
```bash
ssh-keygen -t rsa -b 4096
ssh-copy-id root@154.94.235.178
```

### 2. 配置部署参数

**方式一：直接修改示例配置（快速）**
```bash
# 直接编辑 deploy.env.example
vim deploy.env.example

# 执行部署（脚本会询问是否创建 deploy.env）
./deploy.sh
```

**方式二：创建独立配置（推荐）**
```bash
# 复制配置文件模板
cp deploy.env.example deploy.env

# 编辑配置文件
vim deploy.env

# 执行部署
./deploy.sh
```

**重要**：
- 如果存在 `deploy.env`，脚本会优先使用它
- 如果只有 `deploy.env.example`，脚本会询问是否创建 `deploy.env`
- `deploy.env` 包含敏感信息，已在 `.gitignore` 中排除

### 3. 执行部署

```bash
# 直接执行，脚本会自动加载配置
./deploy.sh
```

## 部署模式

### 远程部署（推荐）

```bash
export DEPLOY_MODE=remote
export SSH_HOST=154.94.235.178
export SSH_USER=root
./deploy.sh
```

### 本地部署

```bash
export DEPLOY_MODE=local
./deploy.sh
```

## 脚本工作流程

1. **检查 Docker**：自动检测并安装 Docker（Ubuntu/Debian）
2. **创建 Docker 网络**：创建 `cdu-snzh-network` 供容器间通信
3. **检查 MySQL**：如果不存在则部署 MySQL 8.0 容器并加入网络
4. **检查 Redis**：如果不存在则部署 Redis 7 容器并加入网络
5. **检查 MinIO**：如果不存在则部署 MinIO 容器并加入网络
6. **构建镜像**：在本地构建 Docker 镜像
7. **推送镜像**：将镜像传输到远程服务器（远程模式）
8. **部署应用**：启动应用容器并配置环境变量
9. **健康检查**：验证服务是否正常运行

## 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DEPLOY_MODE | 部署模式 | remote |
| SSH_HOST | 服务器地址 | 154.94.235.178 |
| SSH_USER | SSH 用户名 | root |
| SSH_PORT | SSH 端口 | 22 |
| MYSQL_PORT | MySQL 端口 | 3306 |
| REDIS_PORT | Redis 端口 | 6379 |
| MINIO_PORT | MinIO API 端口 | 9000 |
| APP_PORT | 应用端口 | 8062 |

## 常见问题

### Q: SSH 连接失败？
A: 确保：
- 服务器 IP 和端口正确
- 已配置 SSH 密钥认证（推荐）：`ssh-copy-id root@154.94.235.178`
- 防火墙允许 SSH 连接

### Q: Docker 安装失败？
A: 脚本仅支持 Ubuntu/Debian 系统自动安装，其他系统需手动安装 Docker。

### Q: 中间件已存在但未运行？
A: 脚本会自动启动已停止的容器，并确保容器加入 Docker 网络。

### Q: 容器间无法通信？
A: 检查所有容器是否在同一个 Docker 网络中：
```bash
docker network inspect cdu-snzh-network
```

### Q: 如何查看应用日志？
A:
```bash
ssh root@154.94.235.178
docker logs -f cdu-snzh-backend
```

### Q: 如何重新部署？
A: 直接再次运行 `./deploy.sh`，脚本会自动停止旧容器并部署新版本。

### Q: 本地需要安装 Docker 吗？
A: 是的。即使是远程部署，本地也需要 Docker 来构建镜像。

## 手动操作

### 查看容器状态
```bash
ssh root@154.94.235.178
docker ps -a
```

### 重启应用
```bash
ssh root@154.94.235.178
docker restart cdu-snzh-backend
```

### 查看应用日志
```bash
ssh root@154.94.235.178
docker logs -f cdu-snzh-backend
```

### 进入容器
```bash
ssh root@154.94.235.178
docker exec -it cdu-snzh-backend bash
```

## 访问地址

部署完成后，可通过以下地址访问：

- **应用首页**: http://154.94.235.178:8062
- **API 文档**: http://154.94.235.178:8062/doc.html
- **健康检查**: http://154.94.235.178:8062/actuator/health
- **MinIO 控制台**: http://154.94.235.178:9001

## 安全建议

⚠️ **重要**：生产环境请务必：
1. 修改所有默认密码
2. 配置防火墙规则（仅开放必要端口：22, 8062）
3. 使用 HTTPS（配置 Nginx 反向代理）
4. 定期备份数据（MySQL、MinIO）
5. **不要将 `deploy.env` 提交到 Git**（已在 .gitignore 中）
6. 使用 SSH 密钥认证而非密码
7. 定期更新 Docker 镜像和系统补丁

## 网络架构

```
外部访问
  ↓
服务器 (154.94.235.178)
  ├─ 端口 8062 → cdu-snzh-backend 容器
  ├─ 端口 3306 → mysql 容器
  ├─ 端口 6379 → redis 容器
  └─ 端口 9000/9001 → minio 容器

Docker 网络 (cdu-snzh-network)
  ├─ cdu-snzh-backend ←→ mysql (通过容器名)
  ├─ cdu-snzh-backend ←→ redis (通过容器名)
  └─ cdu-snzh-backend ←→ minio (通过容器名)
```

## 技术支持

如有问题，请查看：
- 应用日志: `docker logs cdu-snzh-backend`
- 中间件日志: `docker logs mysql/redis/minio`
