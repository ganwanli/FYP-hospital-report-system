#!/bin/bash

# Hospital Report System Security Testing Automation Script
# This script runs various security tests and generates a report

set -e

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SECURITY_DIR="${PROJECT_DIR}/tests/security"
REPORT_DIR="${SECURITY_DIR}/reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="${REPORT_DIR}/security_report_${TIMESTAMP}.html"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Create report directory
mkdir -p "${REPORT_DIR}"

echo -e "${BLUE}=== Hospital Report System Security Testing ===${NC}"
echo "Starting security tests at $(date)"
echo "Report will be saved to: ${REPORT_FILE}"

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
    echo "[$(date +'%H:%M:%S')] ${level}: ${message}" >> "${REPORT_DIR}/security_test.log"
}

# Initialize HTML report
init_html_report() {
    cat > "${REPORT_FILE}" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>Security Test Report - Hospital Report System</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f4f4f4; padding: 20px; border-radius: 5px; }
        .section { margin: 20px 0; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }
        .success { background-color: #d4edda; border-color: #c3e6cb; }
        .warning { background-color: #fff3cd; border-color: #ffeaa7; }
        .danger { background-color: #f8d7da; border-color: #f5c6cb; }
        .info { background-color: #d1ecf1; border-color: #bee5eb; }
        pre { background-color: #f8f9fa; padding: 10px; border-radius: 3px; overflow-x: auto; }
        table { width: 100%; border-collapse: collapse; margin: 10px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .vulnerability { margin: 10px 0; padding: 10px; border-left: 4px solid #dc3545; }
        .pass { color: #28a745; font-weight: bold; }
        .fail { color: #dc3545; font-weight: bold; }
        .skip { color: #6c757d; font-weight: bold; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Security Test Report</h1>
        <p><strong>System:</strong> Hospital Report System</p>
        <p><strong>Test Date:</strong> $(date)</p>
        <p><strong>Report Generated:</strong> $(date)</p>
    </div>
EOF
}

# Add section to HTML report
add_html_section() {
    local title=$1
    local class=${2:-"info"}
    local content=$3
    
    cat >> "${REPORT_FILE}" << EOF
    <div class="section ${class}">
        <h2>${title}</h2>
        ${content}
    </div>
EOF
}

# Finish HTML report
finish_html_report() {
    cat >> "${REPORT_FILE}" << EOF
    <div class="section info">
        <h2>Test Summary</h2>
        <p>Security testing completed at $(date)</p>
        <p>Full test log available at: ${REPORT_DIR}/security_test.log</p>
    </div>
</body>
</html>
EOF
}

# Test 1: Static Code Analysis
run_static_analysis() {
    log_message "INFO" "Running static code analysis..."
    
    local results=""
    local status="success"
    
    # Check if SonarQube is available
    if command -v sonar-scanner &> /dev/null; then
        log_message "INFO" "Running SonarQube analysis..."
        cd "${PROJECT_DIR}"
        
        # Create sonar-project.properties if it doesn't exist
        if [ ! -f "sonar-project.properties" ]; then
            cat > sonar-project.properties << EOF
sonar.projectKey=hospital-report-system
sonar.projectName=Hospital Report System
sonar.projectVersion=1.0
sonar.sources=.
sonar.exclusions=**/node_modules/**,**/target/**,**/*.test.js,**/*.test.tsx
sonar.java.source=17
sonar.java.binaries=backend/target/classes
EOF
        fi
        
        if sonar-scanner > "${REPORT_DIR}/sonar_output.log" 2>&1; then
            results+="<p class='pass'>✓ SonarQube analysis completed successfully</p>"
            log_message "SUCCESS" "SonarQube analysis completed"
        else
            results+="<p class='fail'>✗ SonarQube analysis failed</p>"
            log_message "ERROR" "SonarQube analysis failed"
            status="warning"
        fi
    else
        results+="<p class='skip'>○ SonarQube not available</p>"
        log_message "WARNING" "SonarQube not found, skipping static analysis"
    fi
    
    # Maven dependency check
    if [ -f "${PROJECT_DIR}/backend/pom.xml" ]; then
        log_message "INFO" "Running Maven dependency check..."
        cd "${PROJECT_DIR}/backend"
        
        if mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7 > "${REPORT_DIR}/dependency_check.log" 2>&1; then
            results+="<p class='pass'>✓ No high-risk dependencies found</p>"
            log_message "SUCCESS" "Dependency check passed"
        else
            results+="<p class='fail'>✗ High-risk dependencies found</p>"
            log_message "ERROR" "High-risk dependencies detected"
            status="danger"
        fi
    fi
    
    # NPM audit
    if [ -f "${PROJECT_DIR}/frontend/package.json" ]; then
        log_message "INFO" "Running NPM security audit..."
        cd "${PROJECT_DIR}/frontend"
        
        if npm audit --audit-level=moderate > "${REPORT_DIR}/npm_audit.log" 2>&1; then
            results+="<p class='pass'>✓ No moderate+ npm vulnerabilities found</p>"
            log_message "SUCCESS" "NPM audit passed"
        else
            results+="<p class='fail'>✗ NPM vulnerabilities found</p>"
            log_message "ERROR" "NPM vulnerabilities detected"
            status="danger"
        fi
    fi
    
    add_html_section "Static Code Analysis" "${status}" "${results}"
}

# Test 2: Dynamic Security Testing
run_dynamic_tests() {
    log_message "INFO" "Running dynamic security tests..."
    
    local results=""
    local status="success"
    
    # Check if application is running
    if curl -s "http://localhost:8080/api/health" > /dev/null 2>&1; then
        log_message "INFO" "Application is running, proceeding with dynamic tests..."
        
        # OWASP ZAP Baseline Scan
        if command -v zap-baseline.py &> /dev/null; then
            log_message "INFO" "Running OWASP ZAP baseline scan..."
            
            if zap-baseline.py -t http://localhost:8080 -r "${REPORT_DIR}/zap_report.html" > "${REPORT_DIR}/zap_output.log" 2>&1; then
                results+="<p class='pass'>✓ ZAP baseline scan completed</p>"
                log_message "SUCCESS" "ZAP scan completed"
            else
                results+="<p class='warning'>⚠ ZAP scan found potential issues</p>"
                log_message "WARNING" "ZAP scan found issues"
                status="warning"
            fi
        else
            results+="<p class='skip'>○ OWASP ZAP not available</p>"
            log_message "WARNING" "OWASP ZAP not found"
        fi
        
        # Custom security tests
        log_message "INFO" "Running custom security tests..."
        
        # Test for SQL injection protection
        if curl -s -d '{"username":"admin'\''OR 1=1--","password":"test"}' \
           -H "Content-Type: application/json" \
           "http://localhost:8080/api/auth/login" | grep -q "error\|invalid"; then
            results+="<p class='pass'>✓ SQL injection protection working</p>"
            log_message "SUCCESS" "SQL injection protection verified"
        else
            results+="<p class='fail'>✗ Potential SQL injection vulnerability</p>"
            log_message "ERROR" "SQL injection vulnerability detected"
            status="danger"
        fi
        
        # Test for XSS protection
        if curl -s -d '{"name":"<script>alert('XSS')</script>","description":"test"}' \
           -H "Content-Type: application/json" \
           "http://localhost:8080/api/reports" | grep -v "<script>"; then
            results+="<p class='pass'>✓ XSS protection working</p>"
            log_message "SUCCESS" "XSS protection verified"
        else
            results+="<p class='fail'>✗ Potential XSS vulnerability</p>"
            log_message "ERROR" "XSS vulnerability detected"
            status="danger"
        fi
        
    else
        results+="<p class='skip'>○ Application not running, skipping dynamic tests</p>"
        log_message "WARNING" "Application not running on localhost:8080"
        status="warning"
    fi
    
    add_html_section "Dynamic Security Testing" "${status}" "${results}"
}

# Test 3: Configuration Security
check_security_config() {
    log_message "INFO" "Checking security configuration..."
    
    local results=""
    local status="success"
    
    # Check application.properties for security settings
    if [ -f "${PROJECT_DIR}/backend/src/main/resources/application.properties" ]; then
        local config_file="${PROJECT_DIR}/backend/src/main/resources/application.properties"
        
        # Check for HTTPS configuration
        if grep -q "server.ssl.enabled=true" "${config_file}"; then
            results+="<p class='pass'>✓ HTTPS enabled in configuration</p>"
        else
            results+="<p class='warning'>⚠ HTTPS not explicitly enabled</p>"
            status="warning"
        fi
        
        # Check for security headers
        if grep -q "security.headers" "${config_file}"; then
            results+="<p class='pass'>✓ Security headers configured</p>"
        else
            results+="<p class='warning'>⚠ Security headers not explicitly configured</p>"
            status="warning"
        fi
        
        # Check for session security
        if grep -q "server.servlet.session.cookie.secure=true" "${config_file}"; then
            results+="<p class='pass'>✓ Secure session cookies configured</p>"
        else
            results+="<p class='warning'>⚠ Secure session cookies not configured</p>"
            status="warning"
        fi
        
        # Check for database password encryption
        if grep -q "jasypt\|encrypt" "${config_file}"; then
            results+="<p class='pass'>✓ Password encryption configured</p>"
        else
            results+="<p class='warning'>⚠ Password encryption not detected</p>"
            status="warning"
        fi
        
    else
        results+="<p class='skip'>○ Application properties file not found</p>"
        status="warning"
    fi
    
    # Check for hardcoded secrets
    log_message "INFO" "Scanning for hardcoded secrets..."
    local secret_patterns=(
        "password\s*=\s*['\"][^'\"]*['\"]"
        "secret\s*=\s*['\"][^'\"]*['\"]"
        "api[_-]?key\s*=\s*['\"][^'\"]*['\"]"
        "token\s*=\s*['\"][^'\"]*['\"]"
    )
    
    local secrets_found=false
    for pattern in "${secret_patterns[@]}"; do
        if grep -r -i -E "${pattern}" "${PROJECT_DIR}" --exclude-dir=node_modules --exclude-dir=target --exclude-dir=.git --exclude="*.log" > "${REPORT_DIR}/secrets_scan.log" 2>&1; then
            secrets_found=true
            break
        fi
    done
    
    if [ "$secrets_found" = true ]; then
        results+="<p class='fail'>✗ Potential hardcoded secrets found</p>"
        log_message "ERROR" "Hardcoded secrets detected"
        status="danger"
    else
        results+="<p class='pass'>✓ No hardcoded secrets found</p>"
        log_message "SUCCESS" "No hardcoded secrets detected"
    fi
    
    add_html_section "Security Configuration" "${status}" "${results}"
}

# Test 4: Authentication & Authorization
test_auth_security() {
    log_message "INFO" "Testing authentication and authorization..."
    
    local results=""
    local status="success"
    
    # Test unauthorized access
    if curl -s -o /dev/null -w "%{http_code}" "http://localhost:8080/api/reports" | grep -q "401\|403"; then
        results+="<p class='pass'>✓ Unauthorized access properly blocked</p>"
        log_message "SUCCESS" "Unauthorized access protection verified"
    else
        results+="<p class='fail'>✗ Unauthorized access not properly blocked</p>"
        log_message "ERROR" "Unauthorized access vulnerability"
        status="danger"
    fi
    
    # Test weak password rejection
    local weak_passwords=("123456" "password" "admin" "test")
    local weak_rejected=true
    
    for pwd in "${weak_passwords[@]}"; do
        if curl -s -d "{\"username\":\"testuser\",\"password\":\"${pwd}\",\"email\":\"test@test.com\"}" \
           -H "Content-Type: application/json" \
           "http://localhost:8080/api/auth/register" | grep -q "success.*true"; then
            weak_rejected=false
            break
        fi
    done
    
    if [ "$weak_rejected" = true ]; then
        results+="<p class='pass'>✓ Weak passwords properly rejected</p>"
        log_message "SUCCESS" "Weak password protection verified"
    else
        results+="<p class='fail'>✗ Weak passwords accepted</p>"
        log_message "ERROR" "Weak password vulnerability"
        status="danger"
    fi
    
    # Test JWT token security
    local invalid_token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.invalid"
    
    if curl -s -H "Authorization: Bearer ${invalid_token}" \
       "http://localhost:8080/api/reports" | grep -q "401\|403\|invalid"; then
        results+="<p class='pass'>✓ Invalid JWT tokens properly rejected</p>"
        log_message "SUCCESS" "JWT token validation verified"
    else
        results+="<p class='fail'>✗ Invalid JWT tokens accepted</p>"
        log_message "ERROR" "JWT token vulnerability"
        status="danger"
    fi
    
    add_html_section "Authentication & Authorization" "${status}" "${results}"
}

# Test 5: Data Protection
test_data_protection() {
    log_message "INFO" "Testing data protection measures..."
    
    local results=""
    local status="success"
    
    # Check for sensitive data in logs
    if find "${PROJECT_DIR}" -name "*.log" -type f -exec grep -l -i "password\|secret\|token" {} \; 2>/dev/null | grep -q .; then
        results+="<p class='fail'>✗ Sensitive data found in log files</p>"
        log_message "ERROR" "Sensitive data in logs detected"
        status="danger"
    else
        results+="<p class='pass'>✓ No sensitive data found in logs</p>"
        log_message "SUCCESS" "Log file security verified"
    fi
    
    # Check for proper error handling
    if curl -s "http://localhost:8080/api/nonexistent" | grep -q -v "stack\|exception\|error.*at"; then
        results+="<p class='pass'>✓ Error messages don't expose sensitive information</p>"
        log_message "SUCCESS" "Error handling security verified"
    else
        results+="<p class='warning'>⚠ Error messages may expose sensitive information</p>"
        log_message "WARNING" "Error handling may leak information"
        status="warning"
    fi
    
    # Check database connection security
    if curl -s -d '{"dataSourceId":999,"sql":"SELECT version()"}' \
       -H "Content-Type: application/json" \
       "http://localhost:8080/api/datasources/execute" | grep -q -v "SQLException\|mysql\|postgresql"; then
        results+="<p class='pass'>✓ Database errors don't expose sensitive information</p>"
        log_message "SUCCESS" "Database error handling verified"
    else
        results+="<p class='warning'>⚠ Database errors may expose sensitive information</p>"
        log_message "WARNING" "Database error handling may leak information"
        status="warning"
    fi
    
    add_html_section "Data Protection" "${status}" "${results}"
}

# Generate security recommendations
generate_recommendations() {
    log_message "INFO" "Generating security recommendations..."
    
    local recommendations="
    <h3>Security Hardening Recommendations</h3>
    <ul>
        <li><strong>Enable HTTPS:</strong> Configure SSL/TLS encryption for all communications</li>
        <li><strong>Implement CSP:</strong> Add Content Security Policy headers to prevent XSS</li>
        <li><strong>Use HSTS:</strong> Enable HTTP Strict Transport Security</li>
        <li><strong>Rate Limiting:</strong> Implement API rate limiting to prevent abuse</li>
        <li><strong>Input Validation:</strong> Strengthen input validation and sanitization</li>
        <li><strong>Session Security:</strong> Configure secure session management</li>
        <li><strong>Database Security:</strong> Use encrypted connections and least privilege access</li>
        <li><strong>Logging:</strong> Implement comprehensive security logging and monitoring</li>
        <li><strong>Dependency Management:</strong> Regularly update dependencies and scan for vulnerabilities</li>
        <li><strong>Secrets Management:</strong> Use proper secrets management solutions</li>
    </ul>
    
    <h3>Immediate Actions Required</h3>
    <ul>
        <li>Review and address any FAIL items in the test results above</li>
        <li>Implement missing security configurations</li>
        <li>Schedule regular security testing</li>
        <li>Conduct security training for development team</li>
    </ul>
    
    <h3>Compliance Considerations</h3>
    <ul>
        <li><strong>Healthcare Data:</strong> Ensure HIPAA compliance for patient data</li>
        <li><strong>Data Protection:</strong> Implement GDPR-compliant data handling</li>
        <li><strong>Access Controls:</strong> Maintain audit trails for compliance</li>
        <li><strong>Encryption:</strong> Use appropriate encryption for data at rest and in transit</li>
    </ul>
    "
    
    add_html_section "Security Recommendations" "info" "${recommendations}"
}

# Main execution
main() {
    log_message "INFO" "Starting comprehensive security testing..."
    
    # Initialize HTML report
    init_html_report
    
    # Run all security tests
    run_static_analysis
    run_dynamic_tests
    check_security_config
    test_auth_security
    test_data_protection
    
    # Generate recommendations
    generate_recommendations
    
    # Finish HTML report
    finish_html_report
    
    log_message "SUCCESS" "Security testing completed!"
    log_message "INFO" "Report saved to: ${REPORT_FILE}"
    
    # Open report in browser if available
    if command -v xdg-open &> /dev/null; then
        xdg-open "${REPORT_FILE}"
    elif command -v open &> /dev/null; then
        open "${REPORT_FILE}"
    fi
}

# Run main function
main "$@"