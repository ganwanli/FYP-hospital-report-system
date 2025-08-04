package com.hospital.report.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回结果类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private Integer code;
    private String message;
    private T data;
    
    // 成功返回
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "Success", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    public static <T> Result<T> success() {
        return new Result<>(200, "Success", null);
    }
    
    // 失败返回
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }
    
    // 参数错误
    public static <T> Result<T> paramError(String message) {
        return new Result<>(400, message, null);
    }
    
    // 未授权
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(401, message, null);
    }
    
    // 禁止访问
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(403, message, null);
    }
    
    // 资源不存在
    public static <T> Result<T> notFound(String message) {
        return new Result<>(404, message, null);
    }
}