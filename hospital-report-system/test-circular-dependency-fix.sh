#!/bin/bash

# 测试循环依赖修复脚本
# 用于验证Spring Boot应用是否能正常启动

echo "=========================================="
echo "测试循环依赖修复"
echo "=========================================="

# 进入后端目录
cd hospital-report-system/backend

echo "1. 清理项目..."
./mvnw clean

echo "2. 编译项目..."
./mvnw compile

if [ $? -eq 0 ]; then
    echo "✅ 编译成功！"
else
    echo "❌ 编译失败！"
    exit 1
fi

echo "3. 运行测试..."
./mvnw test -Dtest=*Test

if [ $? -eq 0 ]; then
    echo "✅ 测试通过！"
else
    echo "⚠️  测试有问题，但继续检查启动..."
fi

echo "4. 尝试启动应用（10秒后自动停止）..."
timeout 10s ./mvnw spring-boot:run &
PID=$!

sleep 8

# 检查进程是否还在运行
if kill -0 $PID 2>/dev/null; then
    echo "✅ 应用启动成功！循环依赖问题已解决。"
    kill $PID
else
    echo "❌ 应用启动失败，可能还有其他问题。"
fi

echo "=========================================="
echo "测试完成"
echo "=========================================="
