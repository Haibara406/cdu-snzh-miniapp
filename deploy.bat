@echo off
chcp 65001 >nul
setlocal

REM ==================== Docker Compose 部署脚本 ====================
REM 适用于 Windows 本地部署
REM ================================================================

set "INFO=[94m[INFO][0m"
set "SUCCESS=[92m[SUCCESS][0m"
set "WARNING=[93m[WARNING][0m"
set "ERROR=[91m[ERROR][0m"

if "%1"=="" goto :deploy
if /i "%1"=="deploy" goto :deploy
if /i "%1"=="start" goto :start
if /i "%1"=="stop" goto :stop
if /i "%1"=="restart" goto :restart
if /i "%1"=="logs" goto :logs
if /i "%1"=="status" goto :status
if /i "%1"=="clean" goto :clean
if /i "%1"=="build" goto :build
if /i "%1"=="help" goto :help
echo %ERROR% 未知命令: %1
goto :help

:deploy
echo ==========================================
echo 🚀 CDU-SNZH Docker Compose 部署
echo ==========================================

REM 检查 Docker
echo %INFO% 检查 Docker...
docker --version >nul 2>&1
if errorlevel 1 (
    echo %ERROR% Docker 未安装或未启动
    echo %INFO% 请安装 Docker Desktop: https://www.docker.com/products/docker-desktop
    exit /b 1
)
echo %SUCCESS% Docker 已安装

REM 检查配置文件（自动使用 .env.example）
if not exist ".env" (
    if exist ".env.example" (
        echo %INFO% 使用 .env.example 配置文件
        copy .env.example .env >nul
        echo %SUCCESS% 已自动创建 .env 文件
    ) else (
        echo %ERROR% .env.example 文件不存在
        exit /b 1
    )
) else (
    echo %INFO% 使用现有 .env 配置文件
)

REM 构建镜像
echo %INFO% 构建 Docker 镜像...
cd cdu-snzh
docker build -t haibaraiii/cdu-snzh:latest .
if errorlevel 1 (
    echo %ERROR% 镜像构建失败
    cd ..
    exit /b 1
)
cd ..
echo %SUCCESS% 镜像构建完成

REM 启动服务
echo %INFO% 启动所有服务...
docker-compose up -d
if errorlevel 1 (
    echo %ERROR% 服务启动失败
    exit /b 1
)

echo.
echo %SUCCESS% 部署完成！
echo.
echo ==========================================
echo 📋 访问地址
echo ==========================================
echo 应用首页: http://localhost:8062
echo API文档: http://localhost:8062/doc.html
echo 健康检查: http://localhost:8062/actuator/health
echo MinIO控制台: http://localhost:9001
echo ==========================================
echo.
echo 💡 常用命令:
echo   docker-compose logs -f app    # 查看应用日志
echo   docker-compose ps             # 查看服务状态
echo   docker-compose stop           # 停止所有服务
echo   docker-compose down           # 停止并删除容器
echo ==========================================
goto :end

:start
echo %INFO% 启动所有服务...
docker-compose start
echo %SUCCESS% 服务已启动
goto :end

:stop
echo %INFO% 停止所有服务...
docker-compose stop
echo %SUCCESS% 服务已停止
goto :end

:restart
echo %INFO% 重启所有服务...
docker-compose restart
echo %SUCCESS% 服务已重启
goto :end

:logs
if "%2"=="" (
    docker-compose logs -f
) else (
    docker-compose logs -f %2
)
goto :end

:status
echo.
echo ==========================================
echo 📊 服务状态
echo ==========================================
docker-compose ps
echo ==========================================
goto :end

:clean
echo %WARNING% 这将删除所有容器和数据，是否继续？(Y/N)
set /p response=
if /i "%response%"=="Y" (
    echo %INFO% 清理所有资源...
    docker-compose down -v
    echo %SUCCESS% 清理完成
) else (
    echo %INFO% 取消清理
)
goto :end

:build
echo %INFO% 重新构建镜像...
cd cdu-snzh
docker build -t haibaraiii/cdu-snzh:latest .
if errorlevel 1 (
    echo %ERROR% 镜像构建失败
    cd ..
    exit /b 1
)
cd ..
echo %SUCCESS% 镜像构建完成
echo %INFO% 重启应用容器...
docker-compose up -d --no-deps --build app
echo %SUCCESS% 应用已更新
goto :end

:help
echo CDU-SNZH Docker Compose 部署脚本
echo.
echo 用法: deploy.bat [命令]
echo.
echo 命令:
echo   deploy    - 构建镜像并部署所有服务（默认）
echo   start     - 启动所有服务
echo   stop      - 停止所有服务
echo   restart   - 重启所有服务
echo   logs      - 查看日志（可选服务名，如: deploy.bat logs app）
echo   status    - 查看服务状态
echo   build     - 重新构建并更新应用
echo   clean     - 清理所有容器和数据
echo   help      - 显示帮助信息
echo.
echo 示例:
echo   deploy.bat              # 首次部署
echo   deploy.bat logs app     # 查看应用日志
echo   deploy.bat build        # 更新应用
echo   deploy.bat clean        # 清理所有数据
goto :end

:end
endlocal
