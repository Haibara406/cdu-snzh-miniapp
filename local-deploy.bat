@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ==================== CDU-SNZH Windows 本地部署脚本 ====================
REM 功能：
REM 1. 检查并启动 Docker Desktop
REM 2. 启动本地中间件（MySQL、Redis、MinIO）
REM 3. 使用 Maven 构建并运行 Spring Boot 应用
REM ================================================================

REM 配置变量
set MYSQL_PORT=3306
set REDIS_PORT=6379
set MINIO_PORT=9000
set MINIO_CONSOLE_PORT=9001
set APP_PORT=8062

REM 默认密码（可通过环境变量覆盖）
if not defined MYSQL_ROOT_PASSWORD set MYSQL_ROOT_PASSWORD=Ww249260523..
if not defined MYSQL_DATABASE set MYSQL_DATABASE=cdu_snzh
if not defined MYSQL_USER set MYSQL_USER=snzh
if not defined MYSQL_PASSWORD set MYSQL_PASSWORD=Ww249260523..
if not defined REDIS_PASSWORD set REDIS_PASSWORD=Ww249260523..
if not defined MINIO_ROOT_USER set MINIO_ROOT_USER=haibara
if not defined MINIO_ROOT_PASSWORD set MINIO_ROOT_PASSWORD=Ww249260523..

REM 颜色定义（Windows 10+）
set "INFO=[94m[INFO][0m"
set "SUCCESS=[92m[SUCCESS][0m"
set "WARNING=[93m[WARNING][0m"
set "ERROR=[91m[ERROR][0m"

REM 解析命令
if "%1"=="" goto :start
if /i "%1"=="start" goto :start
if /i "%1"=="stop" goto :stop
if /i "%1"=="restart" goto :restart
if /i "%1"=="clean" goto :clean
if /i "%1"=="status" goto :status
if /i "%1"=="build" goto :build
if /i "%1"=="help" goto :help
echo %ERROR% 未知命令: %1
goto :help

:start
echo ==========================================
echo 🚀 CDU-SNZH Windows 本地部署
echo ==========================================
call :check_docker
call :check_maven
call :create_network
call :start_mysql
call :start_redis
call :start_minio
call :show_status
call :build_app
call :run_app
goto :end

:stop
echo %INFO% 停止所有服务...
docker stop mysql-local redis-local minio-local 2>nul
echo %SUCCESS% 所有服务已停止
goto :end

:restart
call :stop
timeout /t 2 /nobreak >nul
goto :start

:clean
echo %WARNING% 这将删除所有容器和数据，是否继续？(Y/N)
set /p response=
if /i "%response%"=="Y" (
    echo %INFO% 清理所有资源...
    docker stop mysql-local redis-local minio-local 2>nul
    docker rm mysql-local redis-local minio-local 2>nul
    docker volume rm mysql-local-data redis-local-data minio-local-data 2>nul
    docker network rm cdu-snzh-local 2>nul
    echo %SUCCESS% 清理完成
) else (
    echo %INFO% 取消清理
)
goto :end

:status
echo.
echo ==========================================
echo 📊 服务状态
echo ==========================================
docker ps --filter "name=mysql-local" --filter "name=redis-local" --filter "name=minio-local" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
echo ==========================================
goto :end

:build
call :check_maven
call :build_app
goto :end

:help
echo CDU-SNZH Windows 本地部署脚本
echo.
echo 用法: local-deploy.bat [命令]
echo.
echo 命令:
echo   start     - 启动所有服务并运行应用（默认）
echo   stop      - 停止所有服务
echo   restart   - 重启所有服务
echo   clean     - 清理所有容器和数据
echo   status    - 查看服务状态
echo   build     - 仅构建应用
echo   help      - 显示帮助信息
echo.
echo 示例:
echo   local-deploy.bat          # 启动所有服务
echo   local-deploy.bat stop     # 停止所有服务
echo   local-deploy.bat clean    # 清理所有资源
goto :end

REM ==================== 函数定义 ====================

:check_docker
echo %INFO% 检查 Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Docker 未安装或未启动
    echo %INFO% 请安装 Docker Desktop: https://www.docker.com/products/docker-desktop
    exit /b 1
)
echo %SUCCESS% Docker 已安装
exit /b 0

:check_maven
echo %INFO% 检查 Maven...
mvn --version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Maven 未安装
    echo %INFO% 请安装 Maven: https://maven.apache.org/download.cgi
    exit /b 1
)
echo %SUCCESS% Maven 已安装
exit /b 0

:create_network
echo %INFO% 检查 Docker 网络...
docker network ls | findstr "cdu-snzh-local" >nul 2>&1
if errorlevel 1 (
    echo %INFO% 创建 Docker 网络...
    docker network create cdu-snzh-local
    echo %SUCCESS% Docker 网络创建完成
) else (
    echo %SUCCESS% Docker 网络已存在
)
exit /b 0

:start_mysql
echo %INFO% 检查 MySQL 容器...
docker ps -a --format "{{.Names}}" | findstr "^mysql-local$" >nul 2>&1
if errorlevel 1 (
    echo %INFO% 创建并启动 MySQL...
    docker run -d ^
        --name mysql-local ^
        --network cdu-snzh-local ^
        --restart unless-stopped ^
        -p %MYSQL_PORT%:3306 ^
        -e MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD% ^
        -e MYSQL_DATABASE=%MYSQL_DATABASE% ^
        -e MYSQL_USER=%MYSQL_USER% ^
        -e MYSQL_PASSWORD=%MYSQL_PASSWORD% ^
        -v mysql-local-data:/var/lib/mysql ^
        mysql:8.0 ^
        --character-set-server=utf8mb4 ^
        --collation-server=utf8mb4_unicode_ci
    echo %INFO% 等待 MySQL 启动（30秒）...
    timeout /t 30 /nobreak >nul
) else (
    docker ps --format "{{.Names}}" | findstr "^mysql-local$" >nul 2>&1
    if errorlevel 1 (
        echo %INFO% 启动 MySQL...
        docker start mysql-local
        timeout /t 5 /nobreak >nul
    ) else (
        echo %SUCCESS% MySQL 已运行
    )
)
echo %SUCCESS% MySQL 就绪
exit /b 0

:start_redis
echo %INFO% 检查 Redis 容器...
docker ps -a --format "{{.Names}}" | findstr "^redis-local$" >nul 2>&1
if errorlevel 1 (
    echo %INFO% 创建并启动 Redis...
    docker run -d ^
        --name redis-local ^
        --network cdu-snzh-local ^
        --restart unless-stopped ^
        -p %REDIS_PORT%:6379 ^
        -v redis-local-data:/data ^
        redis:7-alpine ^
        redis-server --requirepass %REDIS_PASSWORD% --appendonly yes
) else (
    docker ps --format "{{.Names}}" | findstr "^redis-local$" >nul 2>&1
    if errorlevel 1 (
        echo %INFO% 启动 Redis...
        docker start redis-local
    ) else (
        echo %SUCCESS% Redis 已运行
    )
)
echo %SUCCESS% Redis 就绪
exit /b 0

:start_minio
echo %INFO% 检查 MinIO 容器...
docker ps -a --format "{{.Names}}" | findstr "^minio-local$" >nul 2>&1
if errorlevel 1 (
    echo %INFO% 创建并启动 MinIO...
    docker run -d ^
        --name minio-local ^
        --network cdu-snzh-local ^
        --restart unless-stopped ^
        -p %MINIO_PORT%:9000 ^
        -p %MINIO_CONSOLE_PORT%:9001 ^
        -e MINIO_ROOT_USER=%MINIO_ROOT_USER% ^
        -e MINIO_ROOT_PASSWORD=%MINIO_ROOT_PASSWORD% ^
        -v minio-local-data:/data ^
        minio/minio:latest ^
        server /data --console-address :9001
    echo %INFO% 等待 MinIO 启动（10秒）...
    timeout /t 10 /nobreak >nul
) else (
    docker ps --format "{{.Names}}" | findstr "^minio-local$" >nul 2>&1
    if errorlevel 1 (
        echo %INFO% 启动 MinIO...
        docker start minio-local
    ) else (
        echo %SUCCESS% MinIO 已运行
    )
)
echo %SUCCESS% MinIO 就绪
exit /b 0

:build_app
echo %INFO% 构建应用...
cd cdu-snzh
call mvn clean package -DskipTests
if errorlevel 1 (
    echo %ERROR% 构建失败
    cd ..
    exit /b 1
)
cd ..
echo %SUCCESS% 应用构建完成
exit /b 0

:run_app
echo %INFO% 启动应用...
cd cdu-snzh

set SPRING_PROFILES_ACTIVE=local
set SPRING_DATASOURCE_URL=jdbc:mysql://localhost:%MYSQL_PORT%/%MYSQL_DATABASE%?useUnicode=true^&characterEncoding=utf8^&serverTimezone=Asia/Shanghai^&useSSL=false
set SPRING_DATASOURCE_USERNAME=%MYSQL_USER%
set SPRING_DATASOURCE_PASSWORD=%MYSQL_PASSWORD%
set SPRING_REDIS_HOST=localhost
set SPRING_REDIS_PASSWORD=%REDIS_PASSWORD%
set MINIO_ENDPOINT=http://localhost:%MINIO_PORT%
set MINIO_ACCESS_KEY=%MINIO_ROOT_USER%
set MINIO_SECRET_KEY=%MINIO_ROOT_PASSWORD%

echo %SUCCESS% 应用启动中...
echo %INFO% 访问地址: http://localhost:%APP_PORT%
echo %INFO% API文档: http://localhost:%APP_PORT%/doc.html
echo %INFO% 按 Ctrl+C 停止应用
echo.

call mvn spring-boot:run
cd ..
exit /b 0

:show_status
call :status
exit /b 0

:end
endlocal
