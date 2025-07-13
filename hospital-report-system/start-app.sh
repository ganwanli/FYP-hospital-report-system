#!/bin/bash

# 医院报告管理系统 - 启动脚本
# Hospital Report Management System - Start Script

echo "🏥 医院报告管理系统 - 现代化登录界面"
echo "================================================"
echo ""

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装，请先安装 Node.js (版本 >= 16.0.0)"
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

# 进入前端目录
cd frontend

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

echo "🚀 启动开发服务器..."
echo ""
echo "📋 访问信息:"
echo "   🔗 主页地址: http://localhost:3000"
echo "   🔗 登录页面: http://localhost:3000/login"
echo "   🔗 系统仪表板: http://localhost:3000/dashboard"
echo "   🎨 现代化登录界面已启用"
echo ""
echo "👥 快速登录账户:"
echo "   👨‍💼 系统管理员: admin / 123456"
echo "   👨‍⚕️ 医生: doctor / 123456"
echo "   👩‍⚕️ 护士: nurse / 123456"
echo "   👨‍💻 部门主管: manager / 123456"
echo ""
echo "✨ 现代化登录界面特性:"
echo "   🎨 渐变背景设计 + 现代化UI"
echo "   💫 简洁优雅的交互体验"
echo "   📱 完美的响应式布局设计"
echo "   ⚡ 快速登录功能测试"
echo "   🕒 实时系统状态显示"
echo "   🛡️ 现代化表单和验证"
echo ""
echo "🌍 国际化语言切换:"
echo "   🇨🇳 中文界面支持"
echo "   🇺🇸 英文界面支持"
echo "   🔄 实时语言切换"
echo "   💾 语言设置持久化"
echo "   🌐 浏览器语言自动检测"
echo ""
echo "================================================"
echo "🎯 点击快速登录按钮可自动填入登录信息"
echo "💡 登录成功后自动跳转到系统仪表板"
echo "🔒 支持多角色登录测试功能"
echo "🎨 现代化UI设计和完整的用户体验"
echo "📊 包含完整的Dashboard管理界面"
echo "🌍 支持中英文语言切换功能"
echo "🔄 每个页面右上角都有语言切换按钮"
echo "💾 语言设置自动保存到本地存储"
echo "🌐 支持浏览器语言自动检测"
echo "================================================"
echo ""

# 启动开发服务器
npm run dev
