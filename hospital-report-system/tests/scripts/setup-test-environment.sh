#!/bin/bash

# Hospital Report System - Test Environment Setup Script
# This script sets up the complete testing environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
TEST_DIR="${PROJECT_DIR}/tests"
BACKEND_DIR="${PROJECT_DIR}/backend"
FRONTEND_DIR="${PROJECT_DIR}/frontend"

# Database configuration
DB_NAME="hospital_test"
DB_USER="test_user"
DB_PASSWORD="test_password"
DB_ROOT_PASSWORD="testroot"

echo -e "${BLUE}=== Hospital Report System Test Environment Setup ===${NC}"

# Function to log messages
log_message() {
    local level=$1
    local message=$2
    local color=$NC
    
    case $level in
        "INFO") color=$BLUE ;;
        "SUCCESS") color=$GREEN ;;
        "WARNING") color=$YELLOW ;;
        "ERROR") color=$RED ;;
    esac
    
    echo -e "${color}[$(date +'%H:%M:%S')] ${level}: ${message}${NC}"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to wait for service
wait_for_service() {
    local host=$1
    local port=$2
    local service_name=$3
    local max_attempts=30
    local attempt=1
    
    log_message "INFO" "Waiting for ${service_name} to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if nc -z $host $port 2>/dev/null; then
            log_message "SUCCESS" "${service_name} is ready!"
            return 0
        fi
        
        log_message "INFO" "Attempt ${attempt}/${max_attempts}: ${service_name} not ready yet..."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_message "ERROR" "${service_name} failed to start within timeout"
    return 1
}

# Function to setup test database
setup_test_database() {
    log_message "INFO" "Setting up test database..."
    
    # Check if MySQL is running
    if ! command_exists mysql; then
        log_message "WARNING" "MySQL client not found. Please install MySQL client."
        return 1
    fi
    
    # Check if we can connect to MySQL
    if ! mysql -u root -p${DB_ROOT_PASSWORD} -e "SELECT 1;" >/dev/null 2>&1; then
        log_message "WARNING" "Cannot connect to MySQL. Please ensure MySQL is running and root password is '${DB_ROOT_PASSWORD}'"
        
        # Try to start MySQL using Docker
        if command_exists docker; then
            log_message "INFO" "Starting MySQL using Docker..."
            docker run -d \
                --name mysql-test \
                -e MYSQL_ROOT_PASSWORD=${DB_ROOT_PASSWORD} \
                -e MYSQL_DATABASE=${DB_NAME} \
                -e MYSQL_USER=${DB_USER} \
                -e MYSQL_PASSWORD=${DB_PASSWORD} \
                -p 3306:3306 \
                mysql:8.0
            
            # Wait for MySQL to be ready
            wait_for_service localhost 3306 "MySQL"
        else
            log_message "ERROR" "Docker not found. Please install MySQL or Docker."
            return 1
        fi
    fi
    
    # Create test database and user
    mysql -u root -p${DB_ROOT_PASSWORD} << EOF
CREATE DATABASE IF NOT EXISTS ${DB_NAME} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'%' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'%';
GRANT ALL PRIVILEGES ON ${DB_NAME}_test.* TO '${DB_USER}'@'%';
FLUSH PRIVILEGES;
EOF
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Test database setup completed"
    else
        log_message "ERROR" "Failed to setup test database"
        return 1
    fi
}

# Function to load test data
load_test_data() {
    log_message "INFO" "Loading test data..."
    
    if [ -f "${TEST_DIR}/data/test-data.sql" ]; then
        mysql -u ${DB_USER} -p${DB_PASSWORD} ${DB_NAME} < "${TEST_DIR}/data/test-data.sql"
        
        if [ $? -eq 0 ]; then
            log_message "SUCCESS" "Test data loaded successfully"
        else
            log_message "ERROR" "Failed to load test data"
            return 1
        fi
    else
        log_message "WARNING" "Test data file not found: ${TEST_DIR}/data/test-data.sql"
    fi
}

# Function to setup backend test environment
setup_backend_tests() {
    log_message "INFO" "Setting up backend test environment..."
    
    if [ ! -d "${BACKEND_DIR}" ]; then
        log_message "ERROR" "Backend directory not found: ${BACKEND_DIR}"
        return 1
    fi
    
    cd "${BACKEND_DIR}"
    
    # Check if Maven is installed
    if ! command_exists mvn; then
        log_message "ERROR" "Maven not found. Please install Maven."
        return 1
    fi
    
    # Copy test configuration
    if [ -f "${TEST_DIR}/config/application-test.yml" ]; then
        cp "${TEST_DIR}/config/application-test.yml" "${BACKEND_DIR}/src/main/resources/"
        log_message "SUCCESS" "Test configuration copied"
    fi
    
    # Install dependencies and run tests
    log_message "INFO" "Installing backend dependencies..."
    mvn clean install -DskipTests
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Backend dependencies installed"
    else
        log_message "ERROR" "Failed to install backend dependencies"
        return 1
    fi
    
    # Run unit tests
    log_message "INFO" "Running backend unit tests..."
    mvn test -Dspring.profiles.active=test
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Backend unit tests passed"
    else
        log_message "WARNING" "Some backend unit tests failed"
    fi
}

# Function to setup frontend test environment
setup_frontend_tests() {
    log_message "INFO" "Setting up frontend test environment..."
    
    if [ ! -d "${FRONTEND_DIR}" ]; then
        log_message "ERROR" "Frontend directory not found: ${FRONTEND_DIR}"
        return 1
    fi
    
    cd "${FRONTEND_DIR}"
    
    # Check if Node.js is installed
    if ! command_exists node; then
        log_message "ERROR" "Node.js not found. Please install Node.js."
        return 1
    fi
    
    # Check if npm is installed
    if ! command_exists npm; then
        log_message "ERROR" "npm not found. Please install npm."
        return 1
    fi
    
    # Install dependencies
    log_message "INFO" "Installing frontend dependencies..."
    npm ci
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Frontend dependencies installed"
    else
        log_message "ERROR" "Failed to install frontend dependencies"
        return 1
    fi
    
    # Run unit tests
    log_message "INFO" "Running frontend unit tests..."
    npm run test -- --coverage --watchAll=false
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Frontend unit tests passed"
    else
        log_message "WARNING" "Some frontend unit tests failed"
    fi
}

# Function to setup performance testing tools
setup_performance_tests() {
    log_message "INFO" "Setting up performance testing tools..."
    
    # Check if JMeter is installed
    if command_exists jmeter; then
        log_message "SUCCESS" "JMeter found"
        
        # Validate JMeter test plan
        if [ -f "${TEST_DIR}/jmeter/hospital-report-performance-test.jmx" ]; then
            log_message "SUCCESS" "JMeter test plan found"
        else
            log_message "WARNING" "JMeter test plan not found"
        fi
    else
        log_message "WARNING" "JMeter not found. Please install JMeter for performance testing."
        
        # Try to download JMeter
        if command_exists curl; then
            log_message "INFO" "Downloading JMeter..."
            mkdir -p "${TEST_DIR}/tools"
            cd "${TEST_DIR}/tools"
            
            # Download JMeter (this is just an example, adjust version as needed)
            JMETER_VERSION="5.5"
            JMETER_URL="https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-${JMETER_VERSION}.tgz"
            
            if curl -L -o "apache-jmeter-${JMETER_VERSION}.tgz" "${JMETER_URL}"; then
                tar -xzf "apache-jmeter-${JMETER_VERSION}.tgz"
                export PATH="${TEST_DIR}/tools/apache-jmeter-${JMETER_VERSION}/bin:$PATH"
                log_message "SUCCESS" "JMeter downloaded and setup"
            else
                log_message "WARNING" "Failed to download JMeter"
            fi
        fi
    fi
}

# Function to setup security testing tools
setup_security_tests() {
    log_message "INFO" "Setting up security testing tools..."
    
    # Check for OWASP ZAP
    if command_exists zap-baseline.py; then
        log_message "SUCCESS" "OWASP ZAP found"
    else
        log_message "WARNING" "OWASP ZAP not found. Security scans will be limited."
    fi
    
    # Make security test script executable
    if [ -f "${TEST_DIR}/security/run-security-tests.sh" ]; then
        chmod +x "${TEST_DIR}/security/run-security-tests.sh"
        log_message "SUCCESS" "Security test script made executable"
    fi
    
    # Check for dependency scanning tools
    cd "${BACKEND_DIR}"
    mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=10 >/dev/null 2>&1
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "OWASP Dependency Check available"
    else
        log_message "WARNING" "OWASP Dependency Check may not be properly configured"
    fi
}

# Function to start test services
start_test_services() {
    log_message "INFO" "Starting test services..."
    
    # Start the application in test mode
    cd "${BACKEND_DIR}"
    
    log_message "INFO" "Starting application in test mode..."
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=test > "${TEST_DIR}/logs/app.log" 2>&1 &
    APP_PID=$!
    echo $APP_PID > "${TEST_DIR}/logs/app.pid"
    
    # Wait for application to start
    wait_for_service localhost 8080 "Hospital Report System"
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Application started successfully (PID: $APP_PID)"
    else
        log_message "ERROR" "Application failed to start"
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    log_message "INFO" "Running integration tests..."
    
    # Run Postman tests if Newman is available
    if command_exists newman; then
        log_message "INFO" "Running Postman integration tests..."
        newman run "${TEST_DIR}/postman/hospital-report-system.postman_collection.json" \
               -e "${TEST_DIR}/postman/test-environment.postman_environment.json" \
               --reporters html,cli \
               --reporter-html-export "${TEST_DIR}/reports/postman-report.html"
        
        if [ $? -eq 0 ]; then
            log_message "SUCCESS" "Postman integration tests passed"
        else
            log_message "WARNING" "Some Postman tests failed"
        fi
    else
        log_message "WARNING" "Newman not found. Install with: npm install -g newman"
    fi
    
    # Run backend integration tests
    cd "${BACKEND_DIR}"
    mvn verify -Dspring.profiles.active=test
    
    if [ $? -eq 0 ]; then
        log_message "SUCCESS" "Backend integration tests passed"
    else
        log_message "WARNING" "Some backend integration tests failed"
    fi
}

# Function to generate test reports
generate_test_reports() {
    log_message "INFO" "Generating test reports..."
    
    # Create reports directory
    mkdir -p "${TEST_DIR}/reports"
    
    # Copy backend test reports
    if [ -d "${BACKEND_DIR}/target/site" ]; then
        cp -r "${BACKEND_DIR}/target/site"/* "${TEST_DIR}/reports/"
        log_message "SUCCESS" "Backend test reports copied"
    fi
    
    # Copy frontend test reports
    if [ -d "${FRONTEND_DIR}/coverage" ]; then
        cp -r "${FRONTEND_DIR}/coverage" "${TEST_DIR}/reports/frontend-coverage"
        log_message "SUCCESS" "Frontend test reports copied"
    fi
    
    # Generate summary report
    cat > "${TEST_DIR}/reports/test-summary.html" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Test Summary - Hospital Report System</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f4f4f4; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .success { background-color: #d4edda; }
        .warning { background-color: #fff3cd; }
        .info { background-color: #d1ecf1; }
        a { color: #007bff; text-decoration: none; }
        a:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Test Summary Report</h1>
        <p><strong>System:</strong> Hospital Report System</p>
        <p><strong>Generated:</strong> $(date)</p>
    </div>
    
    <div class="section info">
        <h2>Test Reports</h2>
        <ul>
            <li><a href="./surefire-report.html">Backend Unit Tests</a></li>
            <li><a href="./frontend-coverage/index.html">Frontend Test Coverage</a></li>
            <li><a href="./postman-report.html">API Integration Tests</a></li>
            <li><a href="../security/reports/">Security Test Reports</a></li>
        </ul>
    </div>
    
    <div class="section success">
        <h2>Quick Start</h2>
        <p>To run the complete test suite:</p>
        <pre>./tests/scripts/setup-test-environment.sh</pre>
    </div>
</body>
</html>
EOF
    
    log_message "SUCCESS" "Test summary report generated"
}

# Function to cleanup test environment
cleanup_test_environment() {
    log_message "INFO" "Cleaning up test environment..."
    
    # Stop application if running
    if [ -f "${TEST_DIR}/logs/app.pid" ]; then
        APP_PID=$(cat "${TEST_DIR}/logs/app.pid")
        if kill -0 $APP_PID 2>/dev/null; then
            kill $APP_PID
            log_message "INFO" "Application stopped (PID: $APP_PID)"
        fi
        rm -f "${TEST_DIR}/logs/app.pid"
    fi
    
    # Stop Docker containers if running
    if command_exists docker; then
        docker stop mysql-test 2>/dev/null || true
        docker rm mysql-test 2>/dev/null || true
    fi
    
    log_message "SUCCESS" "Cleanup completed"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 [option]"
    echo ""
    echo "Options:"
    echo "  setup     - Setup complete test environment"
    echo "  database  - Setup test database only"
    echo "  backend   - Setup backend tests only"
    echo "  frontend  - Setup frontend tests only"
    echo "  security  - Setup security tests only"
    echo "  start     - Start test services"
    echo "  test      - Run all tests"
    echo "  cleanup   - Cleanup test environment"
    echo "  help      - Show this help message"
    echo ""
}

# Main execution
main() {
    local action=${1:-"setup"}
    
    # Create necessary directories
    mkdir -p "${TEST_DIR}/logs"
    mkdir -p "${TEST_DIR}/reports"
    
    case $action in
        "setup")
            log_message "INFO" "Setting up complete test environment..."
            setup_test_database
            setup_backend_tests
            setup_frontend_tests
            setup_performance_tests
            setup_security_tests
            load_test_data
            log_message "SUCCESS" "Test environment setup completed!"
            ;;
        "database")
            setup_test_database
            load_test_data
            ;;
        "backend")
            setup_backend_tests
            ;;
        "frontend")
            setup_frontend_tests
            ;;
        "security")
            setup_security_tests
            ;;
        "start")
            start_test_services
            ;;
        "test")
            start_test_services
            run_integration_tests
            generate_test_reports
            ;;
        "cleanup")
            cleanup_test_environment
            ;;
        "help")
            show_usage
            ;;
        *)
            log_message "ERROR" "Unknown option: $action"
            show_usage
            exit 1
            ;;
    esac
}

# Trap cleanup on script exit
trap cleanup_test_environment EXIT

# Run main function
main "$@"