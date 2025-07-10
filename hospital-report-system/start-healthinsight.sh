#!/bin/bash

# 医院报告管理系统 - HealthInsight 前端启动脚本
# Hospital Report Management System - HealthInsight Frontend Start Script

echo "🏥 医院报告管理系统 - HealthInsight 现代化界面"
echo "================================================"
echo ""

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装，请先安装 Node.js (版本 >= 18.0.0)"
    echo "   下载地址: https://nodejs.org/"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "❌ npm 未安装，请先安装 npm"
    exit 1
fi

echo "✅ Node.js 版本: $(node --version)"
echo "✅ npm 版本: $(npm --version)"
echo ""

# 进入 healthinsight 目录
cd healthinsight

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 正在安装项目依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
    echo "✅ 依赖安装完成"
    echo ""
fi

echo "🚀 启动 HealthInsight 开发服务器..."
echo ""
echo "📋 访问信息:"
echo "   🔗 主页地址: http://localhost:3000"
echo "   🔗 登录页面: http://localhost:3000 (自动显示)"
echo "   🔗 系统仪表板: 登录后自动跳转"
echo "   🎨 现代化 HealthInsight 界面已启用"
echo ""
echo "👥 快速登录账户:"
echo "   👨‍💼 系统管理员: admin / 123456"
echo "   👨‍⚕️ 医生: doctor / 123456"
echo "   👩‍⚕️ 护士: nurse / 123456"
echo "   👨‍💻 部门主管: manager / 123456"
echo ""
echo "✨ HealthInsight 现代化界面特性:"
echo "   🎨 Next.js + React 19 + TypeScript"
echo "   💫 Tailwind CSS + shadcn/ui 组件库"
echo "   📱 完美的响应式设计"
echo "   ⚡ 服务端渲染 (SSR) 支持"
echo "   🛡️ 现代化认证和权限管理"
echo "   🔄 实时数据更新"
echo ""
echo "🌍 国际化语言切换:"
echo "   🇨🇳 中文界面支持"
echo "   🇺🇸 英文界面支持"
echo "   🔄 实时语言切换"
echo "   💾 语言设置持久化"
echo ""
echo "🔧 后端集成:"
echo "   🔗 自动连接到后端 API (http://localhost:8080)"
echo "   🔄 API 代理配置已启用"
echo "   🛡️ JWT 认证集成"
echo "   📊 实时数据获取"
echo ""
echo "================================================"
echo "🎯 点击快速登录按钮可自动填入登录信息"
echo "💡 登录成功后自动跳转到系统仪表板"
echo "🔒 支持多角色登录测试功能"
echo "🎨 现代化 UI 设计和完整的用户体验"
echo "📊 包含完整的 Dashboard 管理界面"
echo "🌍 支持中英文语言切换功能"
echo "🔄 每个页面都有语言切换功能"
echo "💾 设置自动保存到本地存储"
echo "🚀 基于 Next.js 的高性能前端框架"
echo "================================================"
echo ""

# 设置环境变量
export BACKEND_URL="http://localhost:8080"

# 启动开发服务器
npm run dev
