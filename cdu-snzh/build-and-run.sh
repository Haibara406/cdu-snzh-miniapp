#!/bin/bash
# CDU-SNZH 后端服务 Docker 构建和运行脚本

set -e

echo "=========================================="
echo "CDU-SNZH Backend Docker 部署脚本"
echo "=========================================="

# 配置变量
IMAGE_NAME="cdu-snzh"
IMAGE_TAG="latest"
CONTAINER_NAME="cdu-snzh-backend"
PORT="8062"

# 数据库配置
DB_HOST="8.156.75.132"
DB_PORT="3306"
DB_NAME="cdu_snzh"
DB_USER="snzh"
DB_PASSWORD="Ww249260523.."

# Redis配置
REDIS_HOST="8.156.75.132"
REDIS_PASSWORD="Ww249260523.."

# MinIO配置
MINIO_ENDPOINT="http://8.156.75.132:9000"
MINIO_ACCESS_KEY="haibara"
MINIO_SECRET_KEY="Ww249260523.."

echo ""
echo "1. 检查是否存在旧容器..."
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
    echo "   发现旧容器，正在停止并删除..."
    docker stop $CONTAINER_NAME 2>/dev/null || true
    docker rm $CONTAINER_NAME 2>/dev/null || true
    echo "   ✓ 旧容器已删除"
else
    echo "   ✓ 没有旧容器"
fi

echo ""
echo "2. 构建Docker镜像..."
docker build -t $IMAGE_NAME:$IMAGE_TAG .
echo "   ✓ 镜像构建完成"

echo ""
echo "3. 启动容器..."
docker run -d \
  --name $CONTAINER_NAME \
  --restart always \
  -p $PORT:$PORT \
  -e SPRING_PROFILES_ACTIVE=local \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai" \
  -e SPRING_DATASOURCE_USERNAME=$DB_USER \
  -e SPRING_DATASOURCE_PASSWORD=$DB_PASSWORD \
  -e SPRING_REDIS_HOST=$REDIS_HOST \
  -e SPRING_REDIS_PASSWORD=$REDIS_PASSWORD \
  -e MINIO_ENDPOINT=$MINIO_ENDPOINT \
  -e MINIO_ACCESS_KEY=$MINIO_ACCESS_KEY \
  -e MINIO_SECRET_KEY=$MINIO_SECRET_KEY \
  $IMAGE_NAME:$IMAGE_TAG

echo "   ✓ 容器已启动"

echo ""
echo "4. 等待应用启动（60秒）..."
sleep 10
for i in {1..10}; do
    echo -n "."
    sleep 5
done
echo ""

echo ""
echo "5. 检查容器状态..."
docker ps | grep $CONTAINER_NAME
echo ""

echo "=========================================="
echo "部署完成！"
echo "=========================================="
echo ""
echo "访问地址："
echo "  - API文档: http://localhost:$PORT/doc.html"
echo "  - 健康检查: http://localhost:$PORT/actuator/health"
echo ""
echo "查看日志："
echo "  docker logs -f $CONTAINER_NAME"
echo ""
echo "停止服务："
echo "  docker stop $CONTAINER_NAME"
echo ""
echo "=========================================="

