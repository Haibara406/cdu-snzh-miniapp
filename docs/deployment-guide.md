# CDU-SNZH 完整部署指南

## 📋 部署架构

```
应用服务 (cdu-snzh-backend:8062)
    ↓
MySQL (3306) + Redis (6379) + MinIO (9000/9001)
```

---

## 🚀 一键部署（推荐）

### Windows 本地部署
```cmd
deploy\deploy-local.bat
```

### 远程服务器部署
```bash
./deploy/deploy-remote.sh
```

详细说明：[deploy/README.md](../deploy/README.md)

---

## 📦 手动部署

### 1. MySQL 部署

```bash
# 创建目录
sudo mkdir -p /opt/mysql/{data,conf,logs}

# 复制配置文件
sudo cp docs/mysql/my.cnf /opt/mysql/conf/my.cnf

# 设置权限
sudo chown -R 999:999 /opt/mysql/{data,logs}
sudo chmod -R 755 /opt/mysql

# 运行容器
docker run -d \
  --name mysql \
  --restart unless-stopped \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=Ww249260523.. \
  -e MYSQL_DATABASE=cdu_snzh \
  -e MYSQL_USER=snzh \
  -e MYSQL_PASSWORD=Ww249260523.. \
  -e TZ=Asia/Shanghai \
  -v /opt/mysql/data:/var/lib/mysql \
  -v /opt/mysql/conf/my.cnf:/etc/mysql/conf.d/my.cnf \
  -v /opt/mysql/logs:/var/log/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

# 等待启动
sleep 30

# 导入数据库
docker exec -i mysql mysql -uroot -pWw249260523.. cdu_snzh < sql/cdu_snzh.sql
```

### 2. Redis 部署

```bash
docker run -d \
  --name redis \
  --restart unless-stopped \
  -p 6379:6379 \
  -v redis-data:/data \
  redis:7-alpine \
  redis-server --requirepass Ww249260523.. --appendonly yes
```

### 3. MinIO 部署

```bash
# 运行容器
docker run -d \
  --name minio \
  --restart unless-stopped \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=haibara \
  -e MINIO_ROOT_PASSWORD=Ww249260523.. \
  -v minio-data:/data \
  minio/minio:latest \
  server /data --console-address ":9001"

# 等待启动
sleep 10
```

**创建 Bucket**：
1. 访问 MinIO 控制台：http://服务器IP:9001
2. 登录账号：`haibara` / `Ww249260523..`
3. 点击 "Buckets" → "Create Bucket"
4. 输入 Bucket 名称：`cdu-snzh`
5. 点击 "Create Bucket"
6. 设置访问策略：选择 Bucket → Access Policy → 设置为 `public`（或根据需求设置）

### 4. 应用部署

```bash
# 构建镜像
cd cdu-snzh
docker build -t haibaraiii/cdu-snzh:latest .

# 运行容器
docker run -d \
  --name cdu-snzh-backend \
  --restart unless-stopped \
  -p 8062:8062 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://mysql:3306/cdu_snzh?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false" \
  -e SPRING_DATASOURCE_USERNAME=snzh \
  -e SPRING_DATASOURCE_PASSWORD=Ww249260523.. \
  -e SPRING_REDIS_HOST=redis \
  -e SPRING_REDIS_PASSWORD=Ww249260523.. \
  -e MINIO_ENDPOINT=http://minio:9000 \
  -e MINIO_ACCESS_KEY=haibara \
  -e MINIO_SECRET_KEY=Ww249260523.. \
  -e WECHAT_APPID=wxbc43db1ef4db8827 \
  -e WECHAT_SECRET=03c8c9e86330e8ed7e0edae80d794756 \
  -e GAODE_MAP_KEY=dae1412666dc00d4f9660b4e558396b1 \
  -e JWT_SECRET='ixXfr?;*bsFmHuF@tzYUAP.S.MT\=oq|cz3bm(+r(g!iW\3)'"'"'ba/RfqsU}=8+UWJ' \
  -e AI_API_KEY=sk-156f588ab5a04ac0918bbb28bcdd7113 \
  haibaraiii/cdu-snzh:latest

# 查看日志
docker logs -f cdu-snzh-backend
```

---

## 🔍 验证部署

```bash
# 检查所有容器
docker ps

# 测试应用
curl http://localhost:8062/actuator/health

# 访问 API 文档
http://服务器IP:8062/doc.html
```

---

## 🔧 常用命令

### 容器管理
```bash
# 启动/停止/重启
docker start mysql redis minio cdu-snzh-backend
docker stop mysql redis minio cdu-snzh-backend
docker restart cdu-snzh-backend

# 查看日志
docker logs -f cdu-snzh-backend
docker logs -f mysql

# 查看资源使用
docker stats
```

### 数据库操作
```bash
# 进入 MySQL
docker exec -it mysql mysql -uroot -pWw249260523..

# 备份数据库
docker exec mysql mysqldump -uroot -pWw249260523.. cdu_snzh > backup.sql

# 恢复数据库
docker exec -i mysql mysql -uroot -pWw249260523.. cdu_snzh < backup.sql
```

### 应用更新
```bash
# 停止旧容器
docker stop cdu-snzh-backend
docker rm cdu-snzh-backend

# 重新构建
cd cdu-snzh
docker build -t haibaraiii/cdu-snzh:latest .

# 启动新容器（使用相同命令）
docker run -d --name cdu-snzh-backend ...
```

---

## ⚠️ 故障排查

### 容器启动失败
```bash
# 查看日志
docker logs 容器名

# 检查端口占用
netstat -tlnp | grep 端口号
```

### 无法连接数据库
```bash
# 检查 MySQL 是否运行
docker ps | grep mysql

# 测试连接
docker exec -it mysql mysql -usnzh -pWw249260523.. -e "SELECT 1"
```

### MinIO Bucket 不存在
1. 访问 http://服务器IP:9001
2. 登录后手动创建 Bucket：`cdu-snzh`

---

## 📊 配置说明

| 服务 | 端口 | 账号 | 密码 |
|------|------|------|------|
| MySQL | 3306 | snzh | Ww249260523.. |
| Redis | 6379 | - | Ww249260523.. |
| MinIO | 9000/9001 | haibara | Ww249260523.. |
| 应用 | 8062 | - | - |

**数据库名称**：`cdu_snzh`
**MinIO Bucket**：`cdu-snzh`

---

**文档版本**: 2.0
**更新日期**: 2025-03-25
