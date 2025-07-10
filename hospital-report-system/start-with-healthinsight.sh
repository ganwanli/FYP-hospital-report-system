#!/bin/bash

# 医院报告管理系统 - 完整系统启动脚本 (使用 HealthInsight 前端)
# Hospital Report Management System - Complete System Start Script (with HealthInsight Frontend)

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 项目路径
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="${PROJECT_DIR}/backend"
HEALTHINSIGHT_DIR="${PROJECT_DIR}/healthinsight"
DATABASE_DIR="${PROJECT_DIR}/database"

# 配置
DB_NAME="hospital_report_system"
DB_USER="root"
DEFAULT_DB_PASSWORD="ganwanli"
BACKEND_PORT=8080
HEALTHINSIGHT_PORT=3000

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

# 检查服务是否运行
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=1
    
    log_info "等待 ${service_name} 启动..."
    
    while [ $attempt -le $max_attempts ]; do
        if nc -z $host $port 2>/dev/null; then
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    echo ""
    return 1
}

# 检查必要的工具
check_prerequisites() {
    log_info "检查系统环境..."
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js 未安装，请先安装 Node.js (版本 >= 18.0.0)"
        log_info "下载地址: https://nodejs.org/"
        exit 1
    fi
    
    # 检查 npm
    if ! command -v npm &> /dev/null; then
        log_error "npm 未安装，请先安装 npm"
        exit 1
    fi
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        log_error "Java 未安装，请先安装 Java JDK 17+"
        exit 1
    fi
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装，请先安装 Apache Maven"
        exit 1
    fi
    
    # 检查 MySQL
    if ! command -v mysql &> /dev/null; then
        log_warning "MySQL 客户端未找到，请确保 MySQL 服务器正在运行"
    fi
    
    log_success "系统环境检查完成"
    echo "  ✅ Node.js: $(node --version)"
    echo "  ✅ npm: $(npm --version)"
    echo "  ✅ Java: $(java -version 2>&1 | head -n 1)"
    echo "  ✅ Maven: $(mvn --version | head -n 1)"
    echo ""
}

# 启动数据库
start_database() {
    log_info "检查数据库连接..."
    
    # 尝试连接数据库
    if mysql -u $DB_USER -p$DEFAULT_DB_PASSWORD -e "USE $DB_NAME;" 2>/dev/null; then
        log_success "数据库连接成功"
        return 0
    fi
    
    log_warning "数据库连接失败，尝试初始化数据库..."
    
    # 创建数据库
    mysql -u $DB_USER -p$DEFAULT_DB_PASSWORD -e "CREATE DATABASE IF NOT EXISTS $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || {
        log_error "数据库创建失败，请检查 MySQL 服务和密码"
        exit 1
    }
    
    # 初始化数据库
    if [ -f "$DATABASE_DIR/init.sql" ]; then
        log_info "初始化数据库结构..."
        mysql -u $DB_USER -p$DEFAULT_DB_PASSWORD $DB_NAME < "$DATABASE_DIR/init.sql" || {
            log_error "数据库初始化失败"
            exit 1
        }
        log_success "数据库初始化完成"
    fi
}

# 启动后端服务
start_backend() {
    log_info "启动后端服务..."
    
    cd "$BACKEND_DIR"
    
    # 安装依赖
    log_info "安装后端依赖..."
    mvn clean install -DskipTests -q
    
    # 启动服务
    log_info "启动后端服务 (端口: ${BACKEND_PORT})..."
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev >/dev/null 2>&1 &
    BACKEND_PID=$!
    echo $BACKEND_PID > "${PROJECT_DIR}/backend.pid"
    
    # 等待服务启动
    if wait_for_service localhost $BACKEND_PORT "后端服务"; then
        log_success "后端服务启动成功 (PID: $BACKEND_PID)"
        log_info "API文档地址: http://localhost:${BACKEND_PORT}/api/swagger-ui.html"
        return 0
    else
        log_error "后端服务启动失败"
        return 1
    fi
}

# 启动 HealthInsight 前端
start_healthinsight() {
    log_info "启动 HealthInsight 前端..."
    
    cd "$HEALTHINSIGHT_DIR"
    
    # 安装依赖
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install || {
            log_error "前端依赖安装失败"
            return 1
        }
    fi
    
    # 设置环境变量
    export BACKEND_URL="http://localhost:${BACKEND_PORT}"
    
    # 启动服务
    log_info "启动 HealthInsight 前端 (端口: ${HEALTHINSIGHT_PORT})..."
    nohup npm run dev >/dev/null 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > "${PROJECT_DIR}/healthinsight.pid"
    
    # 等待服务启动
    if wait_for_service localhost $HEALTHINSIGHT_PORT "HealthInsight 前端"; then
        log_success "HealthInsight 前端启动成功 (PID: $FRONTEND_PID)"
        return 0
    else
        log_error "HealthInsight 前端启动失败"
        return 1
    fi
}

# 显示启动信息
show_startup_info() {
    echo ""
    echo -e "${CYAN}================================================${NC}"
    echo -e "${GREEN}🏥 医院报告管理系统启动成功！${NC}"
    echo -e "${CYAN}================================================${NC}"
    echo ""
    echo -e "${BLUE}📋 访问地址:${NC}"
    echo -e "   🌐 HealthInsight 前端: ${GREEN}http://localhost:${HEALTHINSIGHT_PORT}${NC}"
    echo -e "   🔧 后端 API: ${GREEN}http://localhost:${BACKEND_PORT}/api${NC}"
    echo -e "   📚 API 文档: ${GREEN}http://localhost:${BACKEND_PORT}/api/swagger-ui.html${NC}"
    echo -e "   ❤️ 健康检查: ${GREEN}http://localhost:${BACKEND_PORT}/api/actuator/health${NC}"
    echo ""
    echo -e "${BLUE}👥 测试账户:${NC}"
    echo -e "   👨‍💼 系统管理员: ${YELLOW}admin${NC} / ${YELLOW}123456${NC}"
    echo -e "   👨‍⚕️ 医生: ${YELLOW}doctor${NC} / ${YELLOW}123456${NC}"
    echo -e "   👩‍⚕️ 护士: ${YELLOW}nurse${NC} / ${YELLOW}123456${NC}"
    echo -e "   👨‍💻 部门主管: ${YELLOW}manager${NC} / ${YELLOW}123456${NC}"
    echo ""
    echo -e "${BLUE}✨ HealthInsight 特性:${NC}"
    echo -e "   🎨 Next.js + React 19 + TypeScript"
    echo -e "   💫 Tailwind CSS + shadcn/ui 组件库"
    echo -e "   📱 完美的响应式设计"
    echo -e "   🌍 中英文语言切换"
    echo -e "   🛡️ 现代化认证和权限管理"
    echo ""
    echo -e "${CYAN}================================================${NC}"
    echo -e "${GREEN}🎯 在浏览器中打开 http://localhost:${HEALTHINSIGHT_PORT} 开始使用${NC}"
    echo -e "${CYAN}================================================${NC}"
    echo ""
}

# 主函数
main() {
    echo -e "${CYAN}"
    echo "🏥 医院报告管理系统 - HealthInsight 版本"
    echo "Hospital Report Management System - HealthInsight Edition"
    echo -e "${NC}"
    
    check_prerequisites
    start_database
    start_backend
    start_healthinsight
    show_startup_info
    
    # 保持脚本运行
    log_info "系统正在运行中... 按 Ctrl+C 停止所有服务"
    
    # 捕获中断信号
    trap 'echo ""; log_info "正在停止所有服务..."; kill $(cat backend.pid healthinsight.pid 2>/dev/null) 2>/dev/null; exit 0' INT
    
    # 等待
    while true; do
        sleep 1
    done
}

# 运行主函数
main "$@"
