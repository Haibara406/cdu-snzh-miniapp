# MySQL Docker 部署文档

## 📋 基本信息

| 项目 | 值 |
|------|-----|
| MySQL版本 | 8.0 |
| 容器名称 | mysql-cdu-snzh |
| 端口映射 | 3306:3306 |
| Root密码 | Ww249260523.. |
| 数据库名称 | cdu_snzh |
| 应用用户 | snzh |
| 应用密码 | Ww249260523.. |
| 字符集 | utf8mb4 |

## 📁 目录挂载信息

| 容器内路径 | 宿主机路径 | 说明 |
|-----------|-----------|------|
| /var/lib/mysql | /opt/mysql/data | MySQL数据文件 |
| /etc/mysql/conf.d/my.cnf | /opt/mysql/conf/my.cnf | MySQL配置文件 |
| /var/log/mysql | /opt/mysql/logs | MySQL日志文件 |

## 🚀 快速部署

### 一键部署脚本

```bash
# 1. 创建必要的目录
sudo mkdir -p /opt/mysql/{data,conf,logs}

# 2. 复制配置文件
sudo cp docs/mysql/my.cnf /opt/mysql/conf/my.cnf

# 3. 设置权限（MySQL容器使用uid:gid 999:999）
sudo chown -R 999:999 /opt/mysql/{data,logs}
sudo chown 999:999 /opt/mysql/conf/my.cnf
sudo chmod -R 755 /opt/mysql

# 4. 运行MySQL容器
docker run -d \
  --name mysql-cdu-snzh \
  --restart always \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=Ww249260523.. \
  -e MYSQL_DATABASE=cdu_snzh_miniapp \
  -e MYSQL_USER=snzh \
  -e MYSQL_PASSWORD=Ww249260523.. \
  -e TZ=Asia/Shanghai \
  -v /opt/mysql/data:/var/lib/mysql \
  -v /opt/mysql/conf/my.cnf:/etc/mysql/conf.d/my.cnf \
  -v /opt/mysql/logs:/var/log/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci

# 5. 验证容器运行状态
docker ps | grep mysql-cdu-snzh

# 6. 查看启动日志
docker logs -f mysql-cdu-snzh
```

## 📊 性能配置说明

当前配置针对可用1G内存优化：

- **InnoDB缓冲池**：512MB（最重要的性能参数）
- **最大连接数**：200
- **预计内存占用**：600-800MB

配置文件位置：`docs/mysql/my.cnf`

## 🔧 初始化数据库

### 导入数据库结构

```bash
# 等待MySQL完全启动（约30秒）
sleep 30

# 导入SQL文件
docker exec -i mysql-cdu-snzh mysql -uroot -pWw249260523.. cdu_snzh_miniapp < sql/cdu_snzh_miniapp.sql
```

### 配置远程访问

```bash
# 进入MySQL
docker exec -it mysql-cdu-snzh mysql -uroot -pWw249260523..
```

在MySQL命令行中执行：

```sql
-- 允许snzh用户从任何IP访问
CREATE USER 'snzh'@'%' IDENTIFIED BY 'Ww249260523..';
GRANT ALL PRIVILEGES ON cdu_snzh_miniapp.* TO 'snzh'@'%';
FLUSH PRIVILEGES;
EXIT;
```

## 🔐 开放端口

云服务器需在安全组中添加规则：
- **端口**：3306
- **协议**：TCP
- **来源**：应用服务器IP（推荐）或 0.0.0.0/0

Linux防火墙：
```bash
sudo firewall-cmd --permanent --add-port=3306/tcp
sudo firewall-cmd --reload
```

## 🔧 常用命令

### 容器管理
```bash
# 启动/停止/重启
docker start mysql-cdu-snzh
docker stop mysql-cdu-snzh
docker restart mysql-cdu-snzh

# 查看日志
docker logs -f mysql-cdu-snzh

# 进入MySQL
docker exec -it mysql-cdu-snzh mysql -uroot -pWw249260523..
```

### 数据库备份
```bash
# 备份
docker exec mysql-cdu-snzh mysqldump -uroot -pWw249260523.. cdu_snzh_miniapp > backup_$(date +%Y%m%d).sql

# 恢复
docker exec -i mysql-cdu-snzh mysql -uroot -pWw249260523.. cdu_snzh_miniapp < backup.sql
```

### 监控
```bash
# 查看资源使用
docker stats mysql-cdu-snzh

# 查看连接数
docker exec -it mysql-cdu-snzh mysql -uroot -pWw249260523.. -e "SHOW STATUS LIKE 'Threads_connected';"
```

## 🌐 应用服务器连接配置

### Spring Boot 配置（application.yml）

```yaml
spring:
  datasource:
    url: jdbc:mysql://8.156.75.132:3306/cdu_snzh_miniapp?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: snzh
    password: Ww249260523..
```

### 测试连接
```bash
mysql -h8.156.75.132 -P3306 -usnzh -pWw249260523.. -e "SELECT 'OK' AS status;"
```

## ⚠️ 故障排查

### 容器启动失败
```bash
# 查看日志
docker logs mysql-cdu-snzh

# 检查权限
sudo chown -R 999:999 /opt/mysql/data
```

### 无法远程连接
```bash
# 1. 检查端口
docker port mysql-cdu-snzh

# 2. 检查用户权限
docker exec -it mysql-cdu-snzh mysql -uroot -pWw249260523.. -e "SELECT host, user FROM mysql.user WHERE user='snzh';"

# 3. 检查防火墙
sudo firewall-cmd --list-ports
```

### 调整内存占用
```bash
# 动态调整为256MB（无需重启）
docker exec -it mysql-cdu-snzh mysql -uroot -pWw249260523.. -e "SET GLOBAL innodb_buffer_pool_size = 268435456;"
```

---

**文档版本**: 1.0  
**创建日期**: 2025-10-13

