package com.hospital.report.exception;

/**
 * 系统异常类
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    public SystemException() {
        super();
        this.code = 500;
        this.message = "系统异常";
    }

    public SystemException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    public SystemException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
        this.message = message;
    }

    public SystemException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}