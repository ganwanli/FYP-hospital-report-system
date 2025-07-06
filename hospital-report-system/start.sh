#!/bin/bash

# 医院报表系统快速启动脚本
# Hospital Report System Quick Start Script

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
FRONTEND_DIR="${PROJECT_DIR}/frontend"
DATABASE_DIR="${PROJECT_DIR}/database"

# 配置
DB_NAME="hospital_report_system"
DB_USER="root"
DEFAULT_DB_PASSWORD="ganwanli"
BACKEND_PORT=8080
FRONTEND_PORT=5173

echo -e "${CYAN}"
echo "██╗  ██╗ ██████╗ ███████╗██████╗ ██╗████████╗ █████╗ ██╗     "
echo "██║  ██║██╔═══██╗██╔════╝██╔══██╗██║╚══██╔══╝██╔══██╗██║     "
echo "███████║██║   ██║███████╗██████╔╝██║   ██║   ███████║██║     "
echo "██╔══██║██║   ██║╚════██║██╔═══╝ ██║   ██║   ██╔══██║██║     "
echo "██║  ██║╚██████╔╝███████║██║     ██║   ██║   ██║  ██║███████╗"
echo "╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝     ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝"
echo -e "${NC}"
echo -e "${BLUE}=== 医院报表管理系统快速启动脚本 ===${NC}"
echo ""

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

log_step() {
    echo -e "${PURPLE}[STEP]${NC} $1"
}

# 检查命令是否存在
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # 端口被占用
    else
        return 1  # 端口空闲
    fi
}

# 等待服务启动
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=60
    local attempt=1
    
    log_info "等待 ${service_name} 启动..."
    
    while [ $attempt -le $max_attempts ]; do
        if nc -z $host $port 2>/dev/null; then
            log_success "${service_name} 已启动！"
            return 0
        fi
        
        if [ $((attempt % 10)) -eq 0 ]; then
            log_info "尝试 ${attempt}/${max_attempts}: ${service_name} 启动中..."
        fi
        
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "${service_name} 启动超时"
    return 1
}

# 环境检查
check_environment() {
    log_step "检查系统环境..."
    
    local env_ok=true
    
    # 检查Java
    if command_exists java; then
        local java_version=$(java -version 2>&1 | grep -oP 'version "1\.\K\d+' || java -version 2>&1 | grep -oP 'version "\K\d+')
        if [ "$java_version" -ge 17 ] 2>/dev/null; then
            log_success "Java版本检查通过 ($(java -version 2>&1 | head -n 1))"
        else
            log_error "需要Java 17或更高版本，当前版本：$(java -version 2>&1 | head -n 1)"
            env_ok=false
        fi
    else
        log_error "未找到Java，请安装OpenJDK 17+"
        env_ok=false
    fi
    
    # 检查Maven
    if command_exists mvn; then
        log_success "Maven检查通过 ($(mvn --version | head -n 1))"
    else
        log_error "未找到Maven，请安装Maven 3.6+"
        env_ok=false
    fi
    
    # 检查Node.js
    if command_exists node; then
        local node_version=$(node --version | grep -oP 'v\K\d+')
        if [ "$node_version" -ge 16 ] 2>/dev/null; then
            log_success "Node.js版本检查通过 ($(node --version))"
        else
            log_error "需要Node.js 16或更高版本，当前版本：$(node --version)"
            env_ok=false
        fi
    else
        log_error "未找到Node.js，请安装Node.js 16+"
        env_ok=false
    fi
    
    # 检查npm
    if command_exists npm; then
        log_success "npm检查通过 ($(npm --version))"
    else
        log_error "未找到npm"
        env_ok=false
    fi
    
    # 检查MySQL
    if command_exists mysql; then
        log_success "MySQL客户端检查通过"
    else
        log_warning "未找到MySQL客户端，请确保MySQL已安装并运行"
    fi
    
    if [ "$env_ok" = false ]; then
        log_error "环境检查失败，请安装必要的软件后重试"
        exit 1
    fi
    
    log_success "环境检查完成"
}

# 数据库设置
setup_database() {
    log_step "设置数据库..."
    
    # 询问数据库密码
    read -s -p "请输入MySQL root密码 (默认: ${DEFAULT_DB_PASSWORD}): " db_password
    echo ""
    if [ -z "$db_password" ]; then
        db_password="$DEFAULT_DB_PASSWORD"
    fi
    
    # 测试数据库连接
    if mysql -u "$DB_USER" -p"$db_password" -e "SELECT 1;" >/dev/null 2>&1; then
        log_success "数据库连接成功"
    else
        log_error "数据库连接失败，请检查MySQL服务和密码"
        return 1
    fi
    
    # 创建数据库
    log_info "创建数据库 ${DB_NAME}..."
    mysql -u "$DB_USER" -p"$db_password" << EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EOF
    
    # 执行初始化脚本
    if [ -f "${DATABASE_DIR}/init.sql" ]; then
        log_info "执行数据库初始化脚本..."
        mysql -u "$DB_USER" -p"$db_password" "$DB_NAME" < "${DATABASE_DIR}/init.sql"
        log_success "数据库初始化完成"
    else
        log_warning "未找到数据库初始化脚本"
    fi
    
    # 更新后端配置文件
    log_info "更新后端数据库配置..."
    if [ -f "${BACKEND_DIR}/src/main/resources/application.yml" ]; then
        # 备份原配置文件
        cp "${BACKEND_DIR}/src/main/resources/application.yml" "${BACKEND_DIR}/src/main/resources/application.yml.backup"
        
        # 更新密码
        sed -i.tmp "s/password: ganwanli/password: ${db_password}/g" "${BACKEND_DIR}/src/main/resources/application.yml"
        rm -f "${BACKEND_DIR}/src/main/resources/application.yml.tmp"
        
        log_success "数据库配置更新完成"
    fi
}

# 启动后端
start_backend() {
    log_step "启动后端服务..."
    
    # 检查端口占用
    if check_port $BACKEND_PORT; then
        log_warning "端口 ${BACKEND_PORT} 已被占用"
        read -p "是否终止占用进程并继续？(y/n): " kill_process
        if [ "$kill_process" = "y" ] || [ "$kill_process" = "Y" ]; then
            local pid=$(lsof -ti:$BACKEND_PORT)
            kill -9 $pid 2>/dev/null || true
            log_info "已终止占用端口的进程"
            sleep 2
        else
            log_error "无法启动后端服务，端口被占用"
            return 1
        fi
    fi
    
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

# 启动前端
start_frontend() {
    log_step "启动前端服务..."
    
    # 检查端口占用
    if check_port $FRONTEND_PORT; then
        log_warning "端口 ${FRONTEND_PORT} 已被占用"
        read -p "是否终止占用进程并继续？(y/n): " kill_process
        if [ "$kill_process" = "y" ] || [ "$kill_process" = "Y" ]; then
            local pid=$(lsof -ti:$FRONTEND_PORT)
            kill -9 $pid 2>/dev/null || true
            log_info "已终止占用端口的进程"
            sleep 2
        else
            log_error "无法启动前端服务，端口被占用"
            return 1
        fi
    fi
    
    cd "$FRONTEND_DIR"
    
    # 安装依赖
    log_info "安装前端依赖..."
    npm install --silent
    
    # 启动服务
    log_info "启动前端服务 (端口: ${FRONTEND_PORT})..."
    nohup npm run dev >/dev/null 2>&1 &
    FRONTEND_PID=$!
    echo $FRONTEND_PID > "${PROJECT_DIR}/frontend.pid"
    
    # 等待服务启动
    if wait_for_service localhost $FRONTEND_PORT "前端服务"; then
        log_success "前端服务启动成功 (PID: $FRONTEND_PID)"
        return 0
    else
        log_error "前端服务启动失败"
        return 1
    fi
}

# 停止服务
stop_services() {
    log_step "停止所有服务..."
    
    # 停止后端
    if [ -f "${PROJECT_DIR}/backend.pid" ]; then
        BACKEND_PID=$(cat "${PROJECT_DIR}/backend.pid")
        if kill -0 $BACKEND_PID 2>/dev/null; then
            kill $BACKEND_PID
            log_info "后端服务已停止 (PID: $BACKEND_PID)"
        fi
        rm -f "${PROJECT_DIR}/backend.pid"
    fi
    
    # 停止前端
    if [ -f "${PROJECT_DIR}/frontend.pid" ]; then
        FRONTEND_PID=$(cat "${PROJECT_DIR}/frontend.pid")
        if kill -0 $FRONTEND_PID 2>/dev/null; then
            kill $FRONTEND_PID
            log_info "前端服务已停止 (PID: $FRONTEND_PID)"
        fi
        rm -f "${PROJECT_DIR}/frontend.pid"
    fi
    
    # 强制停止端口占用的进程
    local backend_pids=$(lsof -ti:$BACKEND_PORT 2>/dev/null || true)
    local frontend_pids=$(lsof -ti:$FRONTEND_PORT 2>/dev/null || true)
    
    if [ -n "$backend_pids" ]; then
        echo "$backend_pids" | xargs kill -9 2>/dev/null || true
    fi
    
    if [ -n "$frontend_pids" ]; then
        echo "$frontend_pids" | xargs kill -9 2>/dev/null || true
    fi
    
    log_success "所有服务已停止"
}

# 显示状态
show_status() {
    log_step "检查服务状态..."
    
    # 检查后端状态
    if check_port $BACKEND_PORT; then
        log_success "后端服务运行中 (端口: ${BACKEND_PORT})"
        if curl -s "http://localhost:${BACKEND_PORT}/api/actuator/health" >/dev/null 2>&1; then
            log_success "后端服务健康检查通过"
        else
            log_warning "后端服务可能未完全启动"
        fi
    else
        log_warning "后端服务未运行"
    fi
    
    # 检查前端状态
    if check_port $FRONTEND_PORT; then
        log_success "前端服务运行中 (端口: ${FRONTEND_PORT})"
    else
        log_warning "前端服务未运行"
    fi
}

# 打开浏览器
open_browser() {
    local url="http://localhost:${FRONTEND_PORT}"
    
    if command_exists open; then
        # macOS
        open "$url"
    elif command_exists xdg-open; then
        # Linux
        xdg-open "$url"
    elif command_exists start; then
        # Windows
        start "$url"
    else
        log_info "请手动打开浏览器访问: $url"
    fi
}

# 显示使用说明
show_usage() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  start     - 启动完整系统 (数据库 + 后端 + 前端)"
    echo "  stop      - 停止所有服务"
    echo "  restart   - 重启所有服务"
    echo "  status    - 查看服务状态"
    echo "  backend   - 仅启动后端服务"
    echo "  frontend  - 仅启动前端服务"
    echo "  database  - 仅设置数据库"
    echo "  check     - 检查环境"
    echo "  help      - 显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start     # 完整启动系统"
    echo "  $0 stop      # 停止所有服务"
    echo "  $0 status    # 查看状态"
    echo ""
}

# 显示启动完成信息
show_completion() {
    echo ""
    echo -e "${GREEN}🎉 系统启动完成！${NC}"
    echo ""
    echo -e "${CYAN}访问地址:${NC}"
    echo -e "  前端应用: ${BLUE}http://localhost:${FRONTEND_PORT}${NC}"
    echo -e "  API文档:  ${BLUE}http://localhost:${BACKEND_PORT}/api/swagger-ui.html${NC}"
    echo -e "  健康检查: ${BLUE}http://localhost:${BACKEND_PORT}/api/actuator/health${NC}"
    echo ""
    echo -e "${CYAN}默认管理员账户:${NC}"
    echo -e "  用户名: ${YELLOW}admin${NC}"
    echo -e "  密码:   ${YELLOW}admin123${NC}"
    echo ""
    echo -e "${CYAN}常用命令:${NC}"
    echo -e "  查看状态: ${YELLOW}$0 status${NC}"
    echo -e "  停止服务: ${YELLOW}$0 stop${NC}"
    echo -e "  重启服务: ${YELLOW}$0 restart${NC}"
    echo ""
    
    # 询问是否打开浏览器
    read -p "是否自动打开浏览器？(y/n): " open_browser_choice
    if [ "$open_browser_choice" = "y" ] || [ "$open_browser_choice" = "Y" ]; then
        log_info "正在打开浏览器..."
        open_browser
    fi
}

# 主函数
main() {
    local action=${1:-"start"}
    
    case $action in
        "start")
            check_environment
            setup_database
            start_backend
            start_frontend
            show_completion
            ;;
        "stop")
            stop_services
            ;;
        "restart")
            stop_services
            sleep 3
            start_backend
            start_frontend
            show_completion
            ;;
        "status")
            show_status
            ;;
        "backend")
            check_environment
            start_backend
            log_success "后端服务启动完成"
            ;;
        "frontend")
            check_environment
            start_frontend
            log_success "前端服务启动完成"
            ;;
        "database")
            setup_database
            ;;
        "check")
            check_environment
            ;;
        "help"|"-h"|"--help")
            show_usage
            ;;
        *)
            log_error "未知选项: $action"
            show_usage
            exit 1
            ;;
    esac
}

# 信号处理
trap stop_services EXIT INT TERM

# 运行主函数
main "$@"