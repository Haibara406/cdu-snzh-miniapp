# 本地部署指南

## 快速开始

### 1. 前置要求

确保本地已安装：
- Docker Desktop
- Maven 3.6+
- JDK 17+

### 2. 一键启动

```bash
./local-deploy.sh
```

脚本会自动：
1. 检查 Docker 和 Maven
2. 创建 Docker 网络
3. 启动 MySQL、Redis、MinIO 容器
4. 构建并运行 Spring Boot 应用

### 3. 访问地址

- 应用首页: http://localhost:8062
- API 文档: http://localhost:8062/doc.html
- 健康检查: http://localhost:8062/actuator/health
- MinIO 控制台: http://localhost:9001

## 常用命令

```bash
# 启动所有服务
./local-deploy.sh start

# 停止所有服务
./local-deploy.sh stop

# 重启所有服务
./local-deploy.sh restart

# 查看服务状态
./local-deploy.sh status

# 仅构建应用
./local-deploy.sh build

# 清理所有容器和数据
./local-deploy.sh clean

# 显示帮助
./local-deploy.sh help
```

## 配置说明

### 默认配置

脚本使用以下默认配置：

| 服务 | 端口 | 用户名 | 密码 |
|------|------|--------|------|
| MySQL | 3306 | snzh | Ww249260523.. |
| Redis | 6379 | - | Ww249260523.. |
| MinIO | 9000/9001 | haibara | Ww249260523.. |
| 应用 | 8062 | - | - |

### 自定义配置

通过环境变量覆盖默认配置：

```bash
export MYSQL_PASSWORD=your_password
export REDIS_PASSWORD=your_password
./local-deploy.sh
```

或创建 `local.env` 文件：

```bash
cp local.env.example local.env
vim local.env
source local.env
./local-deploy.sh
```

## 数据持久化

容器数据存储在 Docker volumes 中：

- `mysql-local-data`: MySQL 数据
- `redis-local-data`: Redis 数据
- `minio-local-data`: MinIO 数据

即使删除容器，数据也会保留。使用 `./local-deploy.sh clean` 可以完全清理。

## 故障排查

### MySQL 连接失败

```bash
# 检查容器状态
docker ps -a | grep mysql-local

# 查看日志
docker logs mysql-local

# 重启容器
docker restart mysql-local
```

### 端口被占用

修改端口配置：

```bash
export MYSQL_PORT=3307
export REDIS_PORT=6380
./local-deploy.sh
```

### 应用启动失败

```bash
# 查看详细日志
cd cdu-snzh
mvn spring-boot:run -X
```

## 开发模式

### 仅启动中间件

如果你想用 IDE 运行应用：

```bash
# 启动中间件
./local-deploy.sh start

# 按 Ctrl+C 停止应用启动
# 然后在 IDE 中运行应用
```

### 热重载

使用 Spring Boot DevTools 实现热重载：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

## 与远程部署的区别

| 特性 | 本地部署 | 远程部署 |
|------|----------|----------|
| 部署方式 | Maven 直接运行 | Docker 容器 |
| 中间件 | 本地 Docker | 远程 Docker |
| 配置文件 | application-local.yml | 环境变量 |
| 适用场景 | 开发调试 | 生产环境 |

## 注意事项

1. 本地部署使用 `application-local.yml` 配置
2. 中间件容器名称带 `-local` 后缀，避免与远程部署冲突
3. 数据卷也带 `-local` 后缀，独立存储
4. 首次启动 MySQL 需要等待约 30 秒初始化
5. 停止应用不会停止中间件容器，需要手动执行 `./local-deploy.sh stop`
