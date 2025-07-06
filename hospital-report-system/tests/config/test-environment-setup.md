# 测试环境配置文档

## 测试环境设置

### 1. 数据库测试环境

#### 1.1 测试数据库配置
```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
  h2:
    console:
      enabled: true
      path: /h2-console

# 测试专用配置
logging:
  level:
    com.hospital.report: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG

# JWT测试配置
jwt:
  secret: testSecretKeyForTestingPurposesOnly
  expiration: 3600000

# 缓存测试配置
cache:
  type: memory
  ttl: 300
```

#### 1.2 测试数据初始化脚本
```sql
-- test-data.sql
-- 测试用户数据
INSERT INTO users (id, username, password, email, real_name, role, status, created_at) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'admin@hospital.com', '系统管理员', 'ADMIN', 'ACTIVE', NOW()),
(2, 'doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'doctor1@hospital.com', '张医生', 'USER', 'ACTIVE', NOW()),
(3, 'nurse1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'nurse1@hospital.com', '李护士', 'USER', 'ACTIVE', NOW()),
(4, 'analyst1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lbdOIGDQR.jSckAta', 'analyst1@hospital.com', '王分析师', 'ANALYST', 'ACTIVE', NOW());

-- 测试数据源
INSERT INTO data_sources (id, name, type, host, port, database_name, username, password, status, created_by, created_at) VALUES
(1, '主数据库', 'MYSQL', 'localhost', 3306, 'hospital_test', 'test_user', 'test_password', 'ACTIVE', 1, NOW()),
(2, '备份数据库', 'POSTGRESQL', 'localhost', 5432, 'hospital_backup', 'backup_user', 'backup_password', 'ACTIVE', 1, NOW());

-- 测试报表配置
INSERT INTO report_configs (id, name, description, data_source_id, canvas_width, canvas_height, components_json, status, created_by, created_at) VALUES
(1, '患者统计报表', '医院患者统计分析报表', 1, 800, 600, '[{"id":"table1","type":"table","name":"患者列表","position":{"x":50,"y":50,"width":700,"height":300},"dataBinding":{"sql":"SELECT * FROM patients","parameters":["startDate","endDate"]}}]', 'ACTIVE', 1, NOW()),
(2, '科室收入报表', '各科室收入统计报表', 1, 1000, 800, '[{"id":"chart1","type":"bar-chart","name":"收入统计","position":{"x":50,"y":50,"width":900,"height":400},"dataBinding":{"sql":"SELECT department, SUM(amount) as total FROM revenue GROUP BY department","parameters":[]}}]', 'ACTIVE', 1, NOW());

-- 测试患者数据
INSERT INTO patients (id, patient_id, name, gender, age, phone, address, department, admission_date, discharge_date, status) VALUES
(1, 'P001', '张三', '男', 35, '13800138001', '北京市朝阳区', '内科', '2023-01-15', '2023-01-20', '已出院'),
(2, 'P002', '李四', '女', 28, '13800138002', '北京市海淀区', '妇科', '2023-01-16', '2023-01-22', '已出院'),
(3, 'P003', '王五', '男', 45, '13800138003', '北京市西城区', '外科', '2023-01-17', NULL, '住院中'),
(4, 'P004', '赵六', '女', 32, '13800138004', '北京市东城区', '儿科', '2023-01-18', '2023-01-25', '已出院'),
(5, 'P005', '钱七', '男', 55, '13800138005', '北京市丰台区', '心血管科', '2023-01-19', NULL, '住院中');

-- 测试收入数据
INSERT INTO revenue (id, department, amount, revenue_date, type, description) VALUES
(1, '内科', 15000.00, '2023-01-15', '门诊费', '门诊收入'),
(2, '外科', 25000.00, '2023-01-15', '手术费', '手术收入'),
(3, '妇科', 18000.00, '2023-01-16', '检查费', '检查收入'),
(4, '儿科', 12000.00, '2023-01-16', '治疗费', '治疗收入'),
(5, '心血管科', 30000.00, '2023-01-17', '手术费', '心脏手术收入');

-- 测试医生数据
INSERT INTO doctors (id, doctor_id, name, department, title, phone, email, status) VALUES
(1, 'D001', '张医生', '内科', '主任医师', '13900139001', 'doctor1@hospital.com', 'ACTIVE'),
(2, 'D002', '李医生', '外科', '副主任医师', '13900139002', 'doctor2@hospital.com', 'ACTIVE'),
(3, 'D003', '王医生', '妇科', '主治医师', '13900139003', 'doctor3@hospital.com', 'ACTIVE'),
(4, 'D004', '刘医生', '儿科', '住院医师', '13900139004', 'doctor4@hospital.com', 'ACTIVE');
```

### 2. 前端测试环境配置

#### 2.1 Jest配置
```javascript
// jest.config.js
module.exports = {
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: ['<rootDir>/src/setupTests.ts'],
  moduleNameMapping: {
    '\\.(css|less|scss|sass)$': 'identity-obj-proxy',
    '^@/(.*)$': '<rootDir>/src/$1',
  },
  collectCoverageFrom: [
    'src/**/*.{ts,tsx}',
    '!src/**/*.d.ts',
    '!src/index.tsx',
    '!src/serviceWorker.ts',
  ],
  coverageThreshold: {
    global: {
      branches: 70,
      functions: 70,
      lines: 70,
      statements: 70,
    },
  },
  transform: {
    '^.+\\.(ts|tsx)$': 'ts-jest',
  },
  testMatch: [
    '<rootDir>/src/**/__tests__/**/*.{ts,tsx}',
    '<rootDir>/src/**/*.{test,spec}.{ts,tsx}',
  ],
};
```

#### 2.2 测试环境变量
```bash
# .env.test
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_MOCK_API=true
REACT_APP_TEST_MODE=true
REACT_APP_LOG_LEVEL=debug
```

#### 2.3 Mock数据配置
```typescript
// src/mocks/handlers.ts
import { rest } from 'msw';

export const handlers = [
  // 认证相关
  rest.post('/api/auth/login', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: {
          token: 'mock-jwt-token',
          user: {
            id: 1,
            username: 'admin',
            email: 'admin@hospital.com',
            role: 'ADMIN',
          },
        },
      })
    );
  }),

  // 数据源相关
  rest.get('/api/datasources', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: [
          {
            id: 1,
            name: '主数据库',
            type: 'MYSQL',
            host: 'localhost',
            port: 3306,
            status: 'ACTIVE',
          },
        ],
      })
    );
  }),

  // 报表相关
  rest.get('/api/reports', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: [
          {
            id: 1,
            name: '患者统计报表',
            description: '医院患者统计分析报表',
            status: 'ACTIVE',
          },
        ],
      })
    );
  }),

  // 报表生成
  rest.post('/api/reports/:id/generate', (req, res, ctx) => {
    return res(
      ctx.json({
        success: true,
        data: {
          reportId: 1,
          reportName: '患者统计报表',
          components: [],
          generatedAt: new Date().toISOString(),
          generationTime: 500,
          fromCache: false,
        },
      })
    );
  }),
];
```

### 3. 集成测试环境

#### 3.1 Docker测试环境
```dockerfile
# Dockerfile.test
FROM openjdk:17-jdk-slim

WORKDIR /app

# 安装测试工具
RUN apt-get update && apt-get install -y \
    curl \
    netcat \
    mysql-client \
    postgresql-client \
    && rm -rf /var/lib/apt/lists/*

# 复制应用
COPY backend/target/hospital-report-system.jar app.jar
COPY tests/ tests/

# 设置测试环境变量
ENV SPRING_PROFILES_ACTIVE=test
ENV DATABASE_URL=jdbc:h2:mem:testdb

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
```

#### 3.2 Docker Compose测试配置
```yaml
# docker-compose.test.yml
version: '3.8'

services:
  app-test:
    build:
      context: .
      dockerfile: Dockerfile.test
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=test
      - DATABASE_URL=jdbc:h2:mem:testdb
    depends_on:
      - test-db
    networks:
      - test-network

  test-db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: testroot
      MYSQL_DATABASE: hospital_test
      MYSQL_USER: test_user
      MYSQL_PASSWORD: test_password
    ports:
      - "3307:3306"
    tmpfs:
      - /var/lib/mysql
    networks:
      - test-network

  frontend-test:
    build:
      context: ./frontend
      dockerfile: Dockerfile.test
    ports:
      - "3001:3000"
    environment:
      - REACT_APP_API_BASE_URL=http://app-test:8080/api
      - CI=true
    depends_on:
      - app-test
    networks:
      - test-network

networks:
  test-network:
    driver: bridge
```

### 4. 性能测试环境

#### 4.1 JMeter测试数据
```bash
# generate-test-data.sh
#!/bin/bash

# 生成大量测试用户
echo "username,password,email" > test_users_large.csv
for i in {1..1000}; do
    echo "user${i},password123,user${i}@test.com" >> test_users_large.csv
done

# 生成测试SQL查询
cat > test_queries.csv << EOF
query_name,sql,description
patient_count,"SELECT COUNT(*) FROM patients WHERE admission_date BETWEEN ? AND ?","患者数量统计"
revenue_summary,"SELECT department, SUM(amount) FROM revenue WHERE revenue_date BETWEEN ? AND ? GROUP BY department","科室收入统计"
doctor_workload,"SELECT d.name, COUNT(p.id) FROM doctors d LEFT JOIN patients p ON d.department = p.department GROUP BY d.id","医生工作量统计"
daily_admissions,"SELECT DATE(admission_date) as date, COUNT(*) FROM patients GROUP BY DATE(admission_date)","每日入院统计"
EOF
```

#### 4.2 负载测试配置
```properties
# jmeter.properties
# JMeter负载测试配置

# 并发用户数
concurrent.users=100

# 测试持续时间（秒）
test.duration=300

# 爬坡时间（秒）
ramp.up.time=60

# 服务器配置
server.host=localhost
server.port=8080
server.protocol=http

# 数据库配置
db.host=localhost
db.port=3306
db.name=hospital_test
db.username=test_user
db.password=test_password

# 测试数据配置
test.data.dir=./test-data
test.users.file=test_users.csv
test.queries.file=test_queries.csv

# 报告配置
report.output.dir=./reports
report.generate.html=true
```

### 5. CI/CD测试环境

#### 5.1 GitHub Actions配置
```yaml
# .github/workflows/test.yml
name: 测试流水线

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    name: 单元测试
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 设置 JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: 设置 Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    
    - name: 后端单元测试
      run: |
        cd backend
        mvn clean test
    
    - name: 前端单元测试
      run: |
        cd frontend
        npm ci
        npm run test:coverage
    
    - name: 上传测试报告
      uses: actions/upload-artifact@v3
      with:
        name: test-reports
        path: |
          backend/target/surefire-reports/
          frontend/coverage/

  integration-tests:
    name: 集成测试
    runs-on: ubuntu-latest
    needs: unit-tests
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: testroot
          MYSQL_DATABASE: hospital_test
          MYSQL_USER: test_user
          MYSQL_PASSWORD: test_password
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 设置 JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: 运行集成测试
      run: |
        cd backend
        mvn clean verify -Pintegration-test
      env:
        DATABASE_URL: jdbc:mysql://localhost:3306/hospital_test
        DATABASE_USERNAME: test_user
        DATABASE_PASSWORD: test_password

  security-tests:
    name: 安全测试
    runs-on: ubuntu-latest
    needs: integration-tests
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 运行 OWASP 依赖检查
      run: |
        cd backend
        mvn org.owasp:dependency-check-maven:check
    
    - name: 运行 npm 安全审计
      run: |
        cd frontend
        npm ci
        npm audit
    
    - name: 上传安全报告
      uses: actions/upload-artifact@v3
      with:
        name: security-reports
        path: |
          backend/target/dependency-check-report.html
```

### 6. 测试数据管理

#### 6.1 测试数据生成器
```java
// TestDataGenerator.java
@Component
@Profile("test")
public class TestDataGenerator {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DataSourceRepository dataSourceRepository;
    
    @Autowired
    private ReportConfigRepository reportConfigRepository;
    
    @EventListener
    @Async
    public void generateTestData(ContextRefreshedEvent event) {
        if (userRepository.count() == 0) {
            generateUsers();
            generateDataSources();
            generateReports();
            generatePatients();
        }
    }
    
    private void generateUsers() {
        // 生成测试用户数据
        List<User> users = Arrays.asList(
            createUser("admin", "admin123", "admin@hospital.com", "ADMIN"),
            createUser("doctor1", "password123", "doctor1@hospital.com", "USER"),
            createUser("nurse1", "password123", "nurse1@hospital.com", "USER"),
            createUser("analyst1", "password123", "analyst1@hospital.com", "ANALYST")
        );
        userRepository.saveAll(users);
    }
    
    private void generateDataSources() {
        // 生成测试数据源
        List<DataSource> dataSources = Arrays.asList(
            createDataSource("主数据库", "MYSQL", "localhost", 3306, "hospital_test"),
            createDataSource("备份数据库", "POSTGRESQL", "localhost", 5432, "hospital_backup")
        );
        dataSourceRepository.saveAll(dataSources);
    }
    
    private void generateReports() {
        // 生成测试报表配置
        DataSource dataSource = dataSourceRepository.findByName("主数据库");
        List<ReportConfig> reports = Arrays.asList(
            createReport("患者统计报表", "医院患者统计分析报表", dataSource),
            createReport("科室收入报表", "各科室收入统计报表", dataSource)
        );
        reportConfigRepository.saveAll(reports);
    }
    
    private void generatePatients() {
        // 生成大量测试患者数据
        Random random = new Random();
        List<Patient> patients = new ArrayList<>();
        
        for (int i = 1; i <= 1000; i++) {
            Patient patient = new Patient();
            patient.setPatientId("P" + String.format("%06d", i));
            patient.setName("患者" + i);
            patient.setGender(random.nextBoolean() ? "男" : "女");
            patient.setAge(random.nextInt(80) + 1);
            patient.setPhone("138" + String.format("%08d", random.nextInt(100000000)));
            patient.setDepartment(getDepartment(random.nextInt(5)));
            patient.setAdmissionDate(generateRandomDate());
            patients.add(patient);
        }
        
        // 批量保存
        patientRepository.saveAll(patients);
    }
    
    // 辅助方法...
}
```

### 7. 测试环境监控

#### 7.1 测试监控配置
```yaml
# prometheus-test.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'hospital-report-test'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s

  - job_name: 'jmeter-test'
    static_configs:
      - targets: ['localhost:9270']
```

#### 7.2 测试日志配置
```xml
<!-- logback-test.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/test.log</file>
        <encoder>
            <pattern>%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.hospital.report" level="DEBUG"/>
    <logger name="org.springframework.test" level="INFO"/>
    <logger name="org.springframework.security" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

这个配置文档提供了完整的测试环境设置指南，包括数据库配置、前端测试环境、集成测试、性能测试和CI/CD配置。