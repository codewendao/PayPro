#!/bin/bash
# ==========================================
# PayPro 一键部署脚本
# 前置要求: Docker 已安装
# 用法: ./install.sh
# ==========================================

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 1. 检测 Docker
if ! command -v docker &> /dev/null; then
    log_error "未检测到 Docker，请先安装 Docker"
    echo "  curl -fsSL https://get.docker.com | bash"
    exit 1
fi
log_info "Docker: $(docker --version)"

# 检查 Docker 权限
if ! docker ps &> /dev/null; then
    log_error "Docker 权限不足，请将当前用户加入 docker 组后重新登录："
    echo ""
    echo "  sudo usermod -aG docker \$USER"
    echo "  newgrp docker"
    echo ""
    echo "或使用 root 用户执行此脚本"
    exit 1
fi

# 2. 安装 Docker Compose（使用项目自带二进制）
if docker compose version &> /dev/null; then
    log_info "Docker Compose 已可用"
elif [ -f "$SCRIPT_DIR/docker-compose" ]; then
    log_info "安装项目自带的 Docker Compose..."

    # 尝试安装为 Docker CLI 插件（Docker 23+）
    DOCKER_CONFIG=${DOCKER_CONFIG:-$HOME/.docker}
    mkdir -p $DOCKER_CONFIG/cli-plugins
    cp "$SCRIPT_DIR/docker-compose" $DOCKER_CONFIG/cli-plugins/docker-compose
    chmod +x $DOCKER_CONFIG/cli-plugins/docker-compose

    if docker compose version &> /dev/null; then
        log_info "Docker Compose 安装完成"
    else
        # 降级：安装为独立 docker-compose（兼容 Docker 20.x）
        log_info "Docker 版本较旧，安装为独立 docker-compose..."
        cp "$SCRIPT_DIR/docker-compose" /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
        if docker-compose --version &> /dev/null; then
            log_info "docker-compose 安装完成"
        else
            log_error "安装失败，请参考 一键启动说明.md 手动安装"
            exit 1
        fi
    fi
else
    log_error "未找到项目自带的 docker-compose 文件"
    exit 1
fi

# 确定使用的命令
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
elif command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
fi

# 3. 创建运行目录
log_info "创建运行目录..."
mkdir -p config qr applogs dump

# 4. 检查配置文件
if [ ! -f "$SCRIPT_DIR/config/application-prod.yml" ]; then
    log_warn "未检测到 config/application-prod.yml，已自动生成模板"
    cat > "$SCRIPT_DIR/config/application-prod.yml" << 'EOF'
# ==========================================
# PayPro 生产环境配置
# 请根据实际情况修改下方配置后重新执行 ./install.sh
# ==========================================

paypro:
  # 外置二维码目录（容器内路径，不用改）
  qr-dir: /app/appsystems/qr
  # 站点访问地址（改为你的域名或IP）
  site: http://localhost:8889
EOF
    echo ""
    log_warn "请先编辑 config/application-prod.yml 修改配置，然后重新执行 ./install.sh"
    exit 0
fi

log_info "配置文件已就绪: config/application-prod.yml"

# 5. 启动
log_info "启动项目（首次需构建镜像，约 3-5 分钟）..."
if ! $COMPOSE_CMD up -d --build; then
    echo ""
    log_error "启动失败！常见原因："
    echo "  1. Docker Hub 连接超时（国内服务器），需配置镜像加速:"
    echo "     sudo tee /etc/docker/daemon.json <<< '{\"registry-mirrors\": [\"https://docker.1ms.run\"]}'"
    echo "     sudo systemctl restart docker"
    echo "  2. Docker 服务未运行: systemctl start docker"
    echo "  3. 端口 3306、6379、8889 被占用: ss -tlnp"
    exit 1
fi

echo ""
echo "========================================="
echo "  PayPro 部署成功！"
echo "  访问: http://IP:8889"
echo "========================================="
echo ""
echo "常用命令:"
echo "  查看日志:  docker compose logs -f pay"
echo "  查看状态:  docker compose ps"
echo "  停止项目:  docker compose down"
echo "  重启项目:  docker compose restart"
