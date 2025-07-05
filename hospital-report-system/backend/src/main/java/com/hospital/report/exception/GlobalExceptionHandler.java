package com.hospital.report.exception;

import com.hospital.report.dto.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

/**
 * 全局异常处理器
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Object>> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.error("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.ok(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 系统异常处理
     */
    @ExceptionHandler(SystemException.class)
    public ResponseEntity<Result<Object>> handleSystemException(SystemException e, HttpServletRequest request) {
        logger.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.ok(Result.error(e.getCode(), e.getMessage()));
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Object>> handleValidationExceptions(MethodArgumentNotValidException e, HttpServletRequest request) {
        logger.error("参数校验异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMessage = new StringBuilder("参数校验失败: ");
        
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            errorMessage.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
            if (i < fieldErrors.size() - 1) {
                errorMessage.append(", ");
            }
        }
        
        return ResponseEntity.ok(Result.error(400, errorMessage.toString()));
    }

    /**
     * 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<Result<Object>> handleBindException(BindException e, HttpServletRequest request) {
        logger.error("绑定异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        StringBuilder errorMessage = new StringBuilder("参数绑定失败: ");
        
        for (int i = 0; i < fieldErrors.size(); i++) {
            FieldError fieldError = fieldErrors.get(i);
            errorMessage.append(fieldError.getField()).append(" ").append(fieldError.getDefaultMessage());
            if (i < fieldErrors.size() - 1) {
                errorMessage.append(", ");
            }
        }
        
        return ResponseEntity.ok(Result.error(400, errorMessage.toString()));
    }

    /**
     * 约束违反异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Object>> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        logger.error("约束违反异常: {} - {}", request.getRequestURI(), e.getMessage());
        
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder errorMessage = new StringBuilder("参数约束违反: ");
        
        int i = 0;
        for (ConstraintViolation<?> violation : violations) {
            errorMessage.append(violation.getPropertyPath()).append(" ").append(violation.getMessage());
            if (i < violations.size() - 1) {
                errorMessage.append(", ");
            }
            i++;
        }
        
        return ResponseEntity.ok(Result.error(400, errorMessage.toString()));
    }

    /**
     * 认证异常处理
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result<Object>> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        logger.error("认证异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "认证失败: " + e.getMessage()));
    }

    /**
     * 凭证错误异常处理
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Result<Object>> handleBadCredentialsException(BadCredentialsException e, HttpServletRequest request) {
        logger.error("凭证错误: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.error(401, "用户名或密码错误"));
    }

    /**
     * 权限不足异常处理
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<Object>> handleAccessDeniedException(AccessDeniedException e, HttpServletRequest request) {
        logger.error("权限不足: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.error(403, "权限不足，访问被拒绝"));
    }

    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Result<Object>> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        logger.error("404异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Result.error(404, "请求的资源不存在"));
    }

    /**
     * 非法参数异常处理
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Object>> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        logger.error("非法参数异常: {} - {}", request.getRequestURI(), e.getMessage());
        return ResponseEntity.ok(Result.error(400, "参数错误: " + e.getMessage()));
    }

    /**
     * 空指针异常处理
     */
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result<Object>> handleNullPointerException(NullPointerException e, HttpServletRequest request) {
        logger.error("空指针异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.ok(Result.error(500, "系统内部错误"));
    }

    /**
     * 运行时异常处理
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Result<Object>> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        logger.error("运行时异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.ok(Result.error(500, "系统运行异常: " + e.getMessage()));
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Object>> handleException(Exception e, HttpServletRequest request) {
        logger.error("未知异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return ResponseEntity.ok(Result.error(500, "系统异常，请联系管理员"));
    }
}