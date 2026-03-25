#!/bin/bash

# ==================== CDU-SNZH 智能部署脚本 ====================
# 功能：
# 1. 检查并安装 Docker
# 2. 检查并部署中间件（MySQL、Redis、MinIO）
# 3. 构建并部署 Spring Boot 应用
# 4. 支持本地和远程部署
# ================================================================

set -e  # 遇到错误立即退出

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_DIR"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 加载配置文件
load_config() {
    local config_file=""

    # 优先级：deploy.env
    if [ -f "$SCRIPT_DIR/deploy.env" ]; then
        config_file="$SCRIPT_DIR/deploy.env"
        log_info "加载配置文件: deploy/deploy.env"
    else
        log_error "配置文件不存在: deploy/deploy.env"
        exit 1
    fi

    source "$config_file"
}

# 验证必需的环境变量
validate_config() {
    local required_vars=(
        "SSH_HOST"
        "SSH_USER"
        "MYSQL_ROOT_PASSWORD"
        "MYSQL_DATABASE"
        "MYSQL_USER"
        "MYSQL_PASSWORD"
        "REDIS_PASSWORD"
        "MINIO_ROOT_USER"
        "MINIO_ROOT_PASSWORD"
    )

    local missing_vars=()
    for var in "${required_vars[@]}"; do
        if [ -z "${!var}" ]; then
            missing_vars+=("$var")
        fi
    done

    if [ ${#missing_vars[@]} -gt 0 ]; then
        log_error "以下必需的环境变量未设置："
        for var in "${missing_vars[@]}"; do
            echo "  - $var"
        done
        log_error "请在 deploy.env 中配置这些变量"
        exit 1
    fi
}

# 配置变量（从环境变量读取，无默认值）
DEPLOY_MODE="${DEPLOY_MODE:-remote}"
SSH_PORT="${SSH_PORT:-22}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
REDIS_PORT="${REDIS_PORT:-6379}"
MINIO_PORT="${MINIO_PORT:-9000}"
MINIO_CONSOLE_PORT="${MINIO_CONSOLE_PORT:-9001}"
APP_NAME="cdu-snzh-backend"
APP_PORT="${APP_PORT:-8062}"
DOCKER_IMAGE="haibaraiii/cdu-snzh:latest"

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 远程执行命令
remote_exec() {
    if [ "$DEPLOY_MODE" = "remote" ]; then
        ssh -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" "$@"
    else
        eval "$@"
    fi
}

# 远程检查命令是否存在
remote_command_exists() {
    if [ "$DEPLOY_MODE" = "remote" ]; then
        ssh -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" "command -v $1 >/dev/null 2>&1"
    else
        command_exists "$1"
    fi
}

# 检查并安装 Docker
check_and_install_docker() {
    log_info "检查 Docker 是否已安装..."

    if remote_command_exists docker; then
        log_success "Docker 已安装"
        remote_exec "docker --version"
        return 0
    fi

    log_warning "Docker 未安装，开始安装..."

    if [ "$DEPLOY_MODE" = "remote" ]; then
        ssh -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" 'bash -s' << 'ENDSSH'
            # 卸载旧版本
            sudo apt-get remove -y docker docker-engine docker.io containerd runc 2>/dev/null || true

            # 更新包索引
            sudo apt-get update

            # 安装依赖
            sudo apt-get install -y ca-certificates curl gnupg lsb-release

            # 添加 Docker 官方 GPG key
            sudo mkdir -p /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

            # 设置仓库
            echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

            # 安装 Docker Engine
            sudo apt-get update
            sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin

            # 启动 Docker
            sudo systemctl start docker
            sudo systemctl enable docker

            echo "Docker 安装完成"
ENDSSH
    else
        log_error "本地模式需要手动安装 Docker"
        exit 1
    fi

    log_success "Docker 安装成功"
}

# 创建 Docker 网络
create_docker_network() {
    log_info "检查 Docker 网络..."

    if remote_exec "docker network ls | grep -q 'cdu-snzh-network'"; then
        log_success "Docker 网络已存在"
        return 0
    fi

    log_info "创建 Docker 网络..."
    remote_exec "docker network create cdu-snzh-network"
    log_success "Docker 网络创建完成"
}

# 检查并部署 MySQL
check_and_deploy_mysql() {
    log_info "检查 MySQL 容器..."

    if remote_exec "docker ps -a --format '{{.Names}}' | grep -q '^mysql$'"; then
        log_success "MySQL 容器已存在"

        if ! remote_exec "docker ps --format '{{.Names}}' | grep -q '^mysql$'"; then
            log_warning "MySQL 容器未运行，正在启动..."
            remote_exec "docker start mysql"
        fi

        # 确保容器在网络中
        if ! remote_exec "docker network inspect cdu-snzh-network | grep -q mysql"; then
            log_info "将 MySQL 加入 Docker 网络..."
            remote_exec "docker network connect cdu-snzh-network mysql 2>/dev/null || true"
        fi
        return 0
    fi

    log_warning "MySQL 容器不存在，开始部署..."

    remote_exec "docker run -d \
        --name mysql \
        --network cdu-snzh-network \
        --restart unless-stopped \
        -p ${MYSQL_PORT}:3306 \
        -e MYSQL_ROOT_PASSWORD='${MYSQL_ROOT_PASSWORD}' \
        -e MYSQL_DATABASE='${MYSQL_DATABASE}' \
        -e MYSQL_USER='${MYSQL_USER}' \
        -e MYSQL_PASSWORD='${MYSQL_PASSWORD}' \
        -v mysql-data:/var/lib/mysql \
        mysql:8.0 \
        --character-set-server=utf8mb4 \
        --collation-server=utf8mb4_unicode_ci"

    log_info "等待 MySQL 启动（30秒）..."
    sleep 30

    log_success "MySQL 部署完成"
}

# 检查并部署 Redis
check_and_deploy_redis() {
    log_info "检查 Redis 容器..."

    if remote_exec "docker ps -a --format '{{.Names}}' | grep -q '^redis$'"; then
        log_success "Redis 容器已存在"

        if ! remote_exec "docker ps --format '{{.Names}}' | grep -q '^redis$'"; then
            log_warning "Redis 容器未运行，正在启动..."
            remote_exec "docker start redis"
        fi

        # 确保容器在网络中
        if ! remote_exec "docker network inspect cdu-snzh-network | grep -q redis"; then
            log_info "将 Redis 加入 Docker 网络..."
            remote_exec "docker network connect cdu-snzh-network redis 2>/dev/null || true"
        fi
        return 0
    fi

    log_warning "Redis 容器不存在，开始部署..."

    remote_exec "docker run -d \
        --name redis \
        --network cdu-snzh-network \
        --restart unless-stopped \
        -p ${REDIS_PORT}:6379 \
        -v redis-data:/data \
        redis:7-alpine \
        redis-server --requirepass '${REDIS_PASSWORD}' --appendonly yes"

    log_success "Redis 部署完成"
}

# 检查并部署 MinIO
check_and_deploy_minio() {
    log_info "检查 MinIO 容器..."

    if remote_exec "docker ps -a --format '{{.Names}}' | grep -q '^minio$'"; then
        log_success "MinIO 容器已存在"

        if ! remote_exec "docker ps --format '{{.Names}}' | grep -q '^minio$'"; then
            log_warning "MinIO 容器未运行，正在启动..."
            remote_exec "docker start minio"
        fi

        # 确保容器在网络中
        if ! remote_exec "docker network inspect cdu-snzh-network | grep -q minio"; then
            log_info "将 MinIO 加入 Docker 网络..."
            remote_exec "docker network connect cdu-snzh-network minio 2>/dev/null || true"
        fi
        return 0
    fi

    log_warning "MinIO 容器不存在，开始部署..."

    remote_exec "docker run -d \
        --name minio \
        --network cdu-snzh-network \
        --restart unless-stopped \
        -p ${MINIO_PORT}:9000 \
        -p ${MINIO_CONSOLE_PORT}:9001 \
        -e MINIO_ROOT_USER='${MINIO_ROOT_USER}' \
        -e MINIO_ROOT_PASSWORD='${MINIO_ROOT_PASSWORD}' \
        -v minio-data:/data \
        minio/minio:latest \
        server /data --console-address ':9001'"

    log_info "等待 MinIO 启动（10秒）..."
    sleep 10

    log_success "MinIO 部署完成"
}

# 构建 Docker 镜像
build_docker_image() {
    log_info "开始构建 Docker 镜像..."

    cd cdu-snzh

    if ! command_exists docker; then
        log_error "本地 Docker 未安装，无法构建镜像"
        exit 1
    fi

    docker build -t "$DOCKER_IMAGE" .

    log_success "Docker 镜像构建完成"

    cd ..
}

# 推送镜像到远程服务器
push_image_to_remote() {
    if [ "$DEPLOY_MODE" = "local" ]; then
        return 0
    fi

    log_info "推送镜像到远程服务器..."

    # 保存镜像为 tar 文件
    docker save "$DOCKER_IMAGE" -o /tmp/cdu-snzh-image.tar

    # 传输到远程服务器
    log_info "传输镜像文件到远程服务器..."
    scp -P "$SSH_PORT" /tmp/cdu-snzh-image.tar "$SSH_USER@$SSH_HOST:/tmp/"

    # 在远程服务器加载镜像
    log_info "在远程服务器加载镜像..."
    remote_exec "docker load -i /tmp/cdu-snzh-image.tar"

    # 清理临时文件
    rm -f /tmp/cdu-snzh-image.tar
    remote_exec "rm -f /tmp/cdu-snzh-image.tar"

    log_success "镜像推送完成"
}

# 部署应用
deploy_application() {
    log_info "部署应用..."

    # 停止并删除旧容器
    if remote_exec "docker ps -a --format '{{.Names}}' | grep -q '^${APP_NAME}$'"; then
        log_info "停止旧容器..."
        remote_exec "docker stop ${APP_NAME} 2>/dev/null || true"
        remote_exec "docker rm ${APP_NAME} 2>/dev/null || true"
    fi

    # 使用容器名作为主机名（Docker 网络内部 DNS）
    MYSQL_HOST="mysql"
    REDIS_HOST="redis"
    MINIO_HOST="minio"

    log_info "使用 Docker 网络内部服务发现"
    log_info "MySQL Host: $MYSQL_HOST"
    log_info "Redis Host: $REDIS_HOST"
    log_info "MinIO Host: $MINIO_HOST"

    # PLACEHOLDER_FOR_APPEND_2
    # 启动新容器
    log_info "启动新容器..."

    remote_exec "docker run -d \
        --name ${APP_NAME} \
        --network cdu-snzh-network \
        --restart unless-stopped \
        -p ${APP_PORT}:${APP_PORT} \
        -v /app/cdu-snzh/logs:/app/logs \
        -e SPRING_PROFILES_ACTIVE=prod \
        -e SPRING_DATASOURCE_URL='jdbc:mysql://${MYSQL_HOST}:3306/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false' \
        -e SPRING_DATASOURCE_USERNAME='${MYSQL_USER}' \
        -e SPRING_DATASOURCE_PASSWORD='${MYSQL_PASSWORD}' \
        -e SPRING_REDIS_HOST='${REDIS_HOST}' \
        -e SPRING_REDIS_PASSWORD='${REDIS_PASSWORD}' \
        -e MINIO_ENDPOINT='http://${MINIO_HOST}:9000' \
        -e MINIO_ACCESS_KEY='${MINIO_ROOT_USER}' \
        -e MINIO_SECRET_KEY='${MINIO_ROOT_PASSWORD}' \
        -e WECHAT_APPID='${WECHAT_APPID}' \
        -e WECHAT_SECRET='${WECHAT_SECRET}' \
        -e WECHAT_MCHID='${WECHAT_MCHID}' \
        -e WECHAT_MCH_SERIAL_NO='${WECHAT_MCH_SERIAL_NO}' \
        -e WECHAT_API_V3_KEY='${WECHAT_API_V3_KEY}' \
        -e WECHAT_NOTIFY_URL='${WECHAT_NOTIFY_URL}' \
        -e WECHAT_REFUND_NOTIFY_URL='${WECHAT_REFUND_NOTIFY_URL}' \
        -e GAODE_MAP_KEY='${GAODE_MAP_KEY}' \
        -e JWT_SECRET='${JWT_SECRET}' \
        -e AI_API_KEY='${AI_API_KEY}' \
        --log-opt max-size=100m \
        --log-opt max-file=5 \
        ${DOCKER_IMAGE}"

    log_info "等待服务启动（20秒）..."
    sleep 20

    # 检查容器状态
    if remote_exec "docker ps | grep -q ${APP_NAME}"; then
        log_success "容器运行正常"
    else
        log_error "容器启动失败，查看日志："
        remote_exec "docker logs --tail 50 ${APP_NAME}"
        exit 1
    fi

    log_success "应用部署完成"
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    for i in {1..10}; do
        if remote_exec "curl -f http://localhost:${APP_PORT}/actuator/health 2>/dev/null"; then
            log_success "服务健康检查通过"
            return 0
        fi
        log_info "等待服务就绪... ($i/10)"
        sleep 3
    done

    log_warning "健康检查超时，但容器正在运行"
}

# 显示部署信息
show_deployment_info() {
    echo ""
    echo "=========================================="
    echo "🎉 CDU-SNZH 部署完成！"
    echo "=========================================="
    echo "🔗 访问地址: http://${SSH_HOST}:${APP_PORT}"
    echo "📚 API文档: http://${SSH_HOST}:${APP_PORT}/doc.html"
    echo "🏥 健康检查: http://${SSH_HOST}:${APP_PORT}/actuator/health"
    echo ""
    echo "中间件信息："
    echo "  MySQL: ${SSH_HOST}:${MYSQL_PORT}"
    echo "  Redis: ${SSH_HOST}:${REDIS_PORT}"
    echo "  MinIO: http://${SSH_HOST}:${MINIO_PORT} (控制台: ${MINIO_CONSOLE_PORT})"
    echo "=========================================="
}

# 主函数
main() {
    echo "=========================================="
    echo "🚀 CDU-SNZH 智能部署脚本"
    echo "=========================================="
    echo ""

    # 加载并验证配置
    load_config
    validate_config

    # 检查部署模式
    log_info "部署模式: $DEPLOY_MODE"
    if [ "$DEPLOY_MODE" = "remote" ]; then
        log_info "目标服务器: $SSH_USER@$SSH_HOST:$SSH_PORT"

        # 测试 SSH 连接
        log_info "测试 SSH 连接..."
        if ! ssh -p "$SSH_PORT" -o ConnectTimeout=5 "$SSH_USER@$SSH_HOST" "echo 'SSH 连接成功'" 2>/dev/null; then
            log_error "SSH 连接失败，请检查配置"
            exit 1
        fi
    fi

    # 步骤 1: 检查并安装 Docker
    check_and_install_docker

    # 步骤 2: 创建 Docker 网络
    create_docker_network

    # 步骤 3: 部署中间件
    check_and_deploy_mysql
    check_and_deploy_redis
    check_and_deploy_minio

    # 步骤 4: 构建镜像
    build_docker_image

    # 步骤 5: 推送镜像（远程模式）
    push_image_to_remote

    # 步骤 6: 部署应用
    deploy_application

    # 步骤 7: 健康检查
    health_check

    # 步骤 8: 显示部署信息
    show_deployment_info
}

# 执行主函数
main "$@"