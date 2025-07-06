# 医院报表系统 - 项目运行指南

## 🚀 快速开始

### 系统要求

#### 环境要求
- **Java**: OpenJDK 17 或更高版本
- **Node.js**: 16.0+ (推荐 18.0+)
- **MySQL**: 8.0+
- **Redis**: 6.0+ (可选，用于缓存)
- **Maven**: 3.6+
- **npm**: 8.0+

#### 硬件要求
- **内存**: 最少4GB，推荐8GB+
- **存储**: 至少2GB可用空间
- **CPU**: 双核2.0GHz+

---

## 📋 第一步：环境检查

### 1.1 检查Java环境
```bash
java -version
# 应该显示 OpenJDK 17 或更高版本
```

### 1.2 检查Node.js环境
```bash
node --version  # 应该 >= 16.0.0
npm --version   # 应该 >= 8.0.0
```

### 1.3 检查Maven环境
```bash
mvn --version
# 应该显示 Maven 3.6+ 和 Java 17+
```

### 1.4 检查MySQL环境
```bash
mysql --version
# 确保MySQL 8.0+正在运行
```

---

## 🗄️ 第二步：数据库设置

### 2.1 创建数据库
```bash
# 登录MySQL
mysql -u root -p

# 执行数据库初始化脚本
source /path/to/hospital-report-system/database/init.sql
```

### 2.2 配置数据库连接
编辑后端配置文件：
```bash
# 编辑 backend/src/main/resources/application.yml
vim backend/src/main/resources/application.yml
```

修改数据库连接信息：
```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/hospital_report_system?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8&useSSL=false
      username: root
      password: your_mysql_password  # 改为你的MySQL密码
```

### 2.3 验证数据库连接
```bash
mysql -u root -p hospital_report_system -e "SHOW TABLES;"
# 应该显示所有系统表
```

---

## 🔧 第三步：后端启动

### 3.1 进入后端目录
```bash
cd hospital-report-system/backend
```

### 3.2 安装依赖
```bash
mvn clean install -DskipTests
```

### 3.3 启动后端服务
```bash
# 方式1：使用Maven插件启动
mvn spring-boot:run

# 方式2：编译后运行JAR包
mvn clean package -DskipTests
java -jar target/hospital-report-system-1.0.0.jar

# 方式3：指定配置文件启动
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### 3.4 验证后端启动
```bash
# 检查服务是否启动
curl http://localhost:8080/api/actuator/health

# 或者在浏览器打开
# http://localhost:8080/api/swagger-ui.html
```

**启动成功标志：**
- 控制台显示 "Started HospitalReportApplication"
- 能够访问 Swagger UI
- Health check 返回 UP 状态

---

## 🎨 第四步：前端启动

### 4.1 进入前端目录
```bash
cd hospital-report-system/frontend
```

### 4.2 安装依赖
```bash
# 清理缓存（如果需要）
npm cache clean --force

# 安装依赖
npm install

# 或者使用 yarn
yarn install
```

### 4.3 启动前端服务
```bash
npm run dev

# 或者
yarn dev
```

### 4.4 验证前端启动
```bash
# 前端通常运行在 http://localhost:5173
# 打开浏览器访问该地址
```

**启动成功标志：**
- 控制台显示 "Local: http://localhost:5173/"
- 浏览器能正常显示登录页面

---

## 🔐 第五步：系统登录

### 5.1 默认管理员账户
```
用户名: admin
密码: admin123
```

### 5.2 创建测试用户（可选）
可以通过管理员账户在用户管理模块创建其他测试用户。

---

## 📊 第六步：功能验证

### 6.1 验证核心功能
1. **用户认证**：登录/注销功能
2. **数据源管理**：创建测试数据源
3. **报表设计**：使用设计器创建简单报表
4. **报表生成**：生成并查看报表
5. **报表导出**：导出PDF/Excel文件

### 6.2 快速测试流程
```bash
# 1. 登录系统
# 2. 进入数据源管理 -> 创建数据源
# 3. 进入报表设计 -> 新建报表
# 4. 拖拽组件到画布
# 5. 配置数据绑定
# 6. 预览报表
# 7. 生成最终报表
```

---

## 🛠️ 故障排除

### 常见问题及解决方案

#### 问题1：后端启动失败 - 数据库连接错误
```bash
# 错误信息：Communications link failure
# 解决方案：
1. 检查MySQL服务是否启动
   systemctl status mysql  # Linux
   brew services list | grep mysql  # macOS
   
2. 检查数据库连接配置
   vim backend/src/main/resources/application.yml
   
3. 测试数据库连接
   mysql -u root -p -h localhost
```

#### 问题2：前端启动失败 - 依赖安装错误
```bash
# 解决方案：
1. 清理 node_modules
   rm -rf node_modules package-lock.json
   
2. 重新安装依赖
   npm install
   
3. 检查 Node.js 版本
   node --version  # 确保 >= 16.0.0
```

#### 问题3：接口调用失败 - CORS错误
```bash
# 检查后端CORS配置
# backend/src/main/resources/application.yml
app:
  security:
    cors:
      allowed-origins: 
        - http://localhost:5173  # 确保包含前端地址
```

#### 问题4：端口冲突
```bash
# 后端端口冲突 (8080)
lsof -i :8080  # 查看端口占用
kill -9 <PID>  # 结束占用进程

# 前端端口冲突 (5173)
# 修改 vite.config.ts 中的端口配置
```

#### 问题5：内存不足
```bash
# 增加Java堆内存
export JAVA_OPTS="-Xmx2g -Xms1g"
mvn spring-boot:run

# 或者在启动脚本中指定
java -Xmx2g -Xms1g -jar target/hospital-report-system-1.0.0.jar
```

---

## 🚀 生产环境部署

### Docker部署（推荐）
```bash
# 构建Docker镜像
docker build -t hospital-report-system .

# 启动服务
docker-compose up -d
```

### 传统部署
```bash
# 1. 编译前端
cd frontend && npm run build

# 2. 编译后端
cd backend && mvn clean package -DskipTests

# 3. 部署到服务器
scp target/hospital-report-system-1.0.0.jar user@server:/opt/hospital-report/
scp -r frontend/dist user@server:/opt/hospital-report/static/

# 4. 启动服务
java -jar -Dspring.profiles.active=prod hospital-report-system-1.0.0.jar
```

---

## 📈 性能优化建议

### 数据库优化
```sql
-- 为经常查询的字段添加索引
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_report_status ON report_config(status);
CREATE INDEX idx_datasource_type ON data_source(type);
```

### 应用优化
```yaml
# application.yml 优化配置
spring:
  datasource:
    hikari:
      maximum-pool-size: 50  # 根据并发量调整
      minimum-idle: 10
  
  redis:
    lettuce:
      pool:
        max-active: 100      # 根据需求调整
```

---

## 📚 开发指南

### 开发环境启动
```bash
# 开发模式启动后端（热重载）
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 开发模式启动前端（热重载）
npm run dev
```

### 代码规范检查
```bash
# 后端代码检查
mvn checkstyle:check

# 前端代码检查
npm run lint
npm run type-check
```

### 测试运行
```bash
# 后端测试
mvn test

# 前端测试
npm run test

# 集成测试
./tests/scripts/setup-test-environment.sh test
```

---

## 📞 技术支持

### 系统监控
- **应用监控**: http://localhost:8080/api/actuator
- **健康检查**: http://localhost:8080/api/actuator/health
- **API文档**: http://localhost:8080/api/swagger-ui.html

### 日志查看
```bash
# 应用日志
tail -f logs/hospital-report-system.log

# 系统日志
journalctl -u hospital-report-system -f
```

### 常用命令
```bash
# 查看系统状态
ps aux | grep java
netstat -tulpn | grep :8080

# 重启服务
systemctl restart hospital-report-system

# 备份数据库
mysqldump -u root -p hospital_report_system > backup.sql
```

---

## 🎯 下一步

1. **熟悉系统功能**：按照功能验证步骤逐个测试
2. **创建示例数据**：添加测试数据源和报表
3. **性能调优**：根据实际使用情况优化配置
4. **定制开发**：根据具体需求进行功能扩展
5. **生产部署**：配置生产环境并部署上线

---

**🎉 恭喜！现在您可以开始使用医院报表系统了！**

如有任何问题，请参考故障排除部分或查看详细的技术文档。