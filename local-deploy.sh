#!/bin/bash

# ==================== CDU-SNZH 本地部署脚本 ====================
# 功能：
# 1. 检查并安装 Docker
# 2. 启动本地中间件（MySQL、Redis、MinIO）
# 3. 使用 Maven 构建并运行 Spring Boot 应用
# 4. 支持快速重启和清理
# ================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 配置变量
MYSQL_PORT=3306
REDIS_PORT=6379
MINIO_PORT=9000
MINIO_CONSOLE_PORT=9001
APP_PORT=8062

# 默认密码（可通过环境变量覆盖）
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-Ww249260523..}"
MYSQL_DATABASE="${MYSQL_DATABASE:-cdu_snzh}"
MYSQL_USER="${MYSQL_USER:-snzh}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-Ww249260523..}"
REDIS_PASSWORD="${REDIS_PASSWORD:-Ww249260523..}"
MINIO_ROOT_USER="${MINIO_ROOT_USER:-haibara}"
MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD:-Ww249260523..}"

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 检查 Docker
check_docker() {
    log_info "检查 Docker..."
    if ! command_exists docker; then
        log_error "Docker 未安装，请先安装 Docker Desktop"
        log_info "下载地址: https://www.docker.com/products/docker-desktop"
        exit 1
    fi
    log_success "Docker 已安装"
}

# 检查 Maven
check_maven() {
    log_info "检查 Maven..."
    if ! command_exists mvn; then
        log_error "Maven 未安装，请先安装 Maven"
        exit 1
    fi
    log_success "Maven 已安装"
}

# 创建 Docker 网络
create_network() {
    log_info "检查 Docker 网络..."
    if docker network ls | grep -q 'cdu-snzh-local'; then
        log_success "Docker 网络已存在"
    else
        log_info "创建 Docker 网络..."
        docker network create cdu-snzh-local
        log_success "Docker 网络创建完成"
    fi
}

# 启动 MySQL
start_mysql() {
    log_info "检查 MySQL 容器..."

    if docker ps -a --format '{{.Names}}' | grep -q '^mysql-local$'; then
        if docker ps --format '{{.Names}}' | grep -q '^mysql-local$'; then
            log_success "MySQL 已运行"
        else
            log_info "启动 MySQL..."
            docker start mysql-local
            sleep 5
        fi
    else
        log_info "创建并启动 MySQL..."
        docker run -d \
            --name mysql-local \
            --network cdu-snzh-local \
            --restart unless-stopped \
            -p ${MYSQL_PORT}:3306 \
            -e MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}" \
            -e MYSQL_DATABASE="${MYSQL_DATABASE}" \
            -e MYSQL_USER="${MYSQL_USER}" \
            -e MYSQL_PASSWORD="${MYSQL_PASSWORD}" \
            -v mysql-local-data:/var/lib/mysql \
            mysql:8.0 \
            --character-set-server=utf8mb4 \
            --collation-server=utf8mb4_unicode_ci

        log_info "等待 MySQL 启动（30秒）..."
        sleep 30
    fi
    log_success "MySQL 就绪"
}

# 启动 Redis
start_redis() {
    log_info "检查 Redis 容器..."

    if docker ps -a --format '{{.Names}}' | grep -q '^redis-local$'; then
        if docker ps --format '{{.Names}}' | grep -q '^redis-local$'; then
            log_success "Redis 已运行"
        else
            log_info "启动 Redis..."
            docker start redis-local
        fi
    else
        log_info "创建并启动 Redis..."
        docker run -d \
            --name redis-local \
            --network cdu-snzh-local \
            --restart unless-stopped \
            -p ${REDIS_PORT}:6379 \
            -v redis-local-data:/data \
            redis:7-alpine \
            redis-server --requirepass "${REDIS_PASSWORD}" --appendonly yes
    fi
    log_success "Redis 就绪"
}

# 启动 MinIO
start_minio() {
    log_info "检查 MinIO 容器..."

    if docker ps -a --format '{{.Names}}' | grep -q '^minio-local$'; then
        if docker ps --format '{{.Names}}' | grep -q '^minio-local$'; then
            log_success "MinIO 已运行"
        else
            log_info "启动 MinIO..."
            docker start minio-local
        fi
    else
        log_info "创建并启动 MinIO..."
        docker run -d \
            --name minio-local \
            --network cdu-snzh-local \
            --restart unless-stopped \
            -p ${MINIO_PORT}:9000 \
            -p ${MINIO_CONSOLE_PORT}:9001 \
            -e MINIO_ROOT_USER="${MINIO_ROOT_USER}" \
            -e MINIO_ROOT_PASSWORD="${MINIO_ROOT_PASSWORD}" \
            -v minio-local-data:/data \
            minio/minio:latest \
            server /data --console-address ':9001'

        log_info "等待 MinIO 启动（10秒）..."
        sleep 10
    fi
    log_success "MinIO 就绪"
}

# 构建应用
build_app() {
    log_info "构建应用..."
    cd cdu-snzh
    mvn clean package -DskipTests
    cd ..
    log_success "应用构建完成"
}

# 运行应用
run_app() {
    log_info "启动应用..."
    cd cdu-snzh

    export SPRING_PROFILES_ACTIVE=local
    export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:${MYSQL_PORT}/${MYSQL_DATABASE}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false"
    export SPRING_DATASOURCE_USERNAME="${MYSQL_USER}"
    export SPRING_DATASOURCE_PASSWORD="${MYSQL_PASSWORD}"
    export SPRING_REDIS_HOST="localhost"
    export SPRING_REDIS_PASSWORD="${REDIS_PASSWORD}"
    export MINIO_ENDPOINT="http://localhost:${MINIO_PORT}"
    export MINIO_ACCESS_KEY="${MINIO_ROOT_USER}"
    export MINIO_SECRET_KEY="${MINIO_ROOT_PASSWORD}"

    log_success "应用启动中..."
    log_info "访问地址: http://localhost:${APP_PORT}"
    log_info "API文档: http://localhost:${APP_PORT}/doc.html"
    log_info "按 Ctrl+C 停止应用"
    echo ""

    mvn spring-boot:run
}

# 停止所有服务
stop_all() {
    log_info "停止所有服务..."
    docker stop mysql-local redis-local minio-local 2>/dev/null || true
    log_success "所有服务已停止"
}

# 清理所有资源
clean_all() {
    log_warning "这将删除所有容器和数据，是否继续？(y/n)"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        log_info "清理所有资源..."
        docker stop mysql-local redis-local minio-local 2>/dev/null || true
        docker rm mysql-local redis-local minio-local 2>/dev/null || true
        docker volume rm mysql-local-data redis-local-data minio-local-data 2>/dev/null || true
        docker network rm cdu-snzh-local 2>/dev/null || true
        log_success "清理完成"
    else
        log_info "取消清理"
    fi
}

# 显示状态
show_status() {
    echo ""
    echo "=========================================="
    echo "📊 服务状态"
    echo "=========================================="
    docker ps --filter "name=mysql-local" --filter "name=redis-local" --filter "name=minio-local" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    echo "=========================================="
}

# 显示帮助
show_help() {
    echo "CDU-SNZH 本地部署脚本"
    echo ""
    echo "用法: ./local-deploy.sh [命令]"
    echo ""
    echo "命令:"
    echo "  start     - 启动所有服务并运行应用（默认）"
    echo "  stop      - 停止所有服务"
    echo "  restart   - 重启所有服务"
    echo "  clean     - 清理所有容器和数据"
    echo "  status    - 查看服务状态"
    echo "  build     - 仅构建应用"
    echo "  help      - 显示帮助信息"
    echo ""
    echo "示例:"
    echo "  ./local-deploy.sh          # 启动所有服务"
    echo "  ./local-deploy.sh stop     # 停止所有服务"
    echo "  ./local-deploy.sh clean    # 清理所有资源"
}

# 主函数
main() {
    case "${1:-start}" in
        start)
            echo "=========================================="
            echo "🚀 CDU-SNZH 本地部署"
            echo "=========================================="
            check_docker
            check_maven
            create_network
            start_mysql
            start_redis
            start_minio
            show_status
            build_app
            run_app
            ;;
        stop)
            stop_all
            ;;
        restart)
            stop_all
            sleep 2
            main start
            ;;
        clean)
            clean_all
            ;;
        status)
            show_status
            ;;
        build)
            check_maven
            build_app
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            log_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

main "$@"
