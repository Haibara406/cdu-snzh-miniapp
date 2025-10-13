# 后端服务 Docker 部署文档

## 📋 基本信息

| 项目 | 值 |
|------|-----|
| 应用名称 | cdu-snzh |
| 容器名称 | cdu-snzh-backend |
| 端口映射 | 8062:8062 |
| JVM内存 | 256m-512m |
| 镜像名称 | cdu-snzh:latest |

## 🚀 快速部署

### 方式一：使用本地已有配置文件

如果你已经在本地修改好了 `application-local.yml` 等配置文件：

```bash
# 1. 进入项目目录
cd cdu-snzh

# 2. 构建Docker镜像
docker build -t cdu-snzh:latest .

# 3. 运行容器
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  cdu-snzh:latest

# 4. 查看日志
docker logs -f cdu-snzh-backend
```

### 方式二：使用环境变量（推荐用于生产环境）

使用环境变量覆盖配置，无需修改配置文件：

```bash
# 构建镜像
docker build -t cdu-snzh:latest .

# 运行容器（使用环境变量）
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://8.156.75.132:3306/cdu_snzh?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_USERNAME=snzh \
  -e SPRING_DATASOURCE_PASSWORD=Ww249260523.. \
  -e SPRING_REDIS_HOST=8.156.75.132 \
  -e SPRING_REDIS_PASSWORD=Ww249260523.. \
  -e MINIO_ENDPOINT=http://8.156.75.132:9000 \
  -e MINIO_ACCESS_KEY=haibara \
  -e MINIO_SECRET_KEY=Ww249260523.. \
  cdu-snzh:latest

# 查看日志
docker logs -f cdu-snzh-backend
```

### 方式三：挂载外部配置文件

如果需要灵活修改配置：

```bash
# 1. 创建配置目录
mkdir -p /opt/cdu-snzh/config

# 2. 复制配置文件到服务器
cp src/main/resources/application-local.yml /opt/cdu-snzh/config/

# 3. 运行容器并挂载配置
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  -v /opt/cdu-snzh/config/application-local.yml:/app/config/application-local.yml:ro \
  -e SPRING_PROFILES_ACTIVE=local \
  cdu-snzh:latest
```

## 📊 镜像信息

- **基础镜像**：eclipse-temurin:17-jre（完整版，非Alpine）
- **镜像大小**：约300MB
- **运行内存**：256-512MB（由JVM参数控制）

**说明**：使用完整版镜像确保所有系统库完整，支持AI嵌入模型等功能。

## 📊 性能配置

### JVM内存调整

默认配置：`-Xms256m -Xmx512m`

如果需要调整内存：

```bash
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  -e JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC" \
  cdu-snzh:latest
```

### 资源限制

限制容器资源使用：

```bash
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  --memory="1g" \
  --cpus="2" \
  cdu-snzh:latest
```

## 🔧 构建优化

### 国内加速构建

如果构建慢，使用Maven国内镜像：

创建 `cdu-snzh/.m2/settings.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings>
  <mirrors>
    <mirror>
      <id>aliyun</id>
      <mirrorOf>central</mirrorOf>
      <name>Aliyun Maven</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
```

修改Dockerfile，在构建阶段添加：

```dockerfile
COPY .m2/settings.xml /root/.m2/settings.xml
```

### 使用构建缓存

```bash
# 使用BuildKit加速构建
DOCKER_BUILDKIT=1 docker build -t cdu-snzh:latest .
```

## 🔍 验证部署

### 检查容器状态

```bash
# 查看运行状态
docker ps | grep cdu-snzh-backend

# 查看资源使用
docker stats cdu-snzh-backend

# 查看详细信息
docker inspect cdu-snzh-backend
```

### 查看日志

```bash
# 实时查看日志
docker logs -f cdu-snzh-backend

# 查看最近100行
docker logs --tail 100 cdu-snzh-backend

# 查看带时间戳的日志
docker logs -f -t cdu-snzh-backend
```

### 健康检查

```bash
# 检查健康状态
docker inspect --format='{{.State.Health.Status}}' cdu-snzh-backend

# 手动测试健康检查
curl http://localhost:8062/actuator/health
```

### 测试API

```bash
# 访问Swagger文档
http://服务器IP:8062/doc.html

# 测试健康检查接口
curl http://服务器IP:8062/actuator/health
```

## 🔧 常用命令

### 容器管理

```bash
# 启动
docker start cdu-snzh-backend

# 停止
docker stop cdu-snzh-backend

# 重启
docker restart cdu-snzh-backend

# 删除容器
docker stop cdu-snzh-backend
docker rm cdu-snzh-backend

# 进入容器
docker exec -it cdu-snzh-backend sh
```

### 镜像管理

```bash
# 查看镜像
docker images | grep cdu-snzh

# 删除镜像
docker rmi cdu-snzh:latest

# 导出镜像
docker save cdu-snzh:latest -o cdu-snzh-latest.tar

# 导入镜像
docker load -i cdu-snzh-latest.tar

# 推送到私有仓库
docker tag cdu-snzh:latest registry.example.com/cdu-snzh:latest
docker push registry.example.com/cdu-snzh:latest
```

## 🔄 更新部署

```bash
# 1. 停止并删除旧容器
docker stop cdu-snzh-backend
docker rm cdu-snzh-backend

# 2. 重新构建镜像
docker build -t cdu-snzh:latest .

# 3. 启动新容器
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  cdu-snzh:latest

# 或使用一键脚本
docker stop cdu-snzh-backend && \
docker rm cdu-snzh-backend && \
docker build -t cdu-snzh:latest . && \
docker run -d --name cdu-snzh-backend --restart always -p 8062:8062 cdu-snzh:latest
```

## 🐳 Docker Compose（可选）

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  backend:
    build: ./cdu-snzh
    image: cdu-snzh:latest
    container_name: cdu-snzh-backend
    restart: always
    ports:
      - "8062:8062"
    environment:
      - SPRING_PROFILES_ACTIVE=local
      - SPRING_DATASOURCE_URL=jdbc:mysql://8.156.75.132:3306/cdu_snzh?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      - SPRING_DATASOURCE_USERNAME=snzh
      - SPRING_DATASOURCE_PASSWORD=Ww249260523..
      - SPRING_REDIS_HOST=8.156.75.132
      - SPRING_REDIS_PASSWORD=Ww249260523..
      - MINIO_ENDPOINT=http://8.156.75.132:9000
      - MINIO_ACCESS_KEY=haibara
      - MINIO_SECRET_KEY=Ww249260523..
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8062/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
```

使用方式：

```bash
# 启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down

# 重新构建并启动
docker-compose up -d --build
```

## ⚠️ 故障排查

### 容器启动失败

```bash
# 查看详细日志
docker logs cdu-snzh-backend

# 检查端口占用
netstat -tlnp | grep 8062

# 检查容器状态
docker ps -a | grep cdu-snzh-backend
```

### 内存不足

```bash
# 减小JVM内存
docker run -d \
  --name cdu-snzh-backend \
  --restart always \
  -p 8062:8062 \
  -e JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseG1GC" \
  cdu-snzh:latest
```

### 无法连接MySQL

```bash
# 进入容器测试网络
docker exec -it cdu-snzh-backend sh
wget -O- http://8.156.75.132:3306

# 检查环境变量
docker exec -it cdu-snzh-backend env | grep SPRING
```

## 📝 注意事项

1. **健康检查依赖**：需要在 `pom.xml` 中添加 Spring Boot Actuator：
   ```xml
   <dependency>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-actuator</artifactId>
   </dependency>
   ```

2. **配置文件优先级**：
   - 环境变量 > 挂载的配置文件 > 镜像内配置文件

3. **安全建议**：
   - 生产环境使用环境变量而非配置文件存储密码
   - 不要将敏感配置文件提交到代码仓库
   - 使用非root用户运行（已在Dockerfile中配置）

4. **时区设置**：
   - 已在镜像中设置为 `Asia/Shanghai`
   - 应用时间和日志时间都会使用中国时区

---

**文档版本**: 1.0  
**创建日期**: 2025-10-13

