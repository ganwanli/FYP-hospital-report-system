package com.hospital.report.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.report.dto.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证失败处理器
 * 
 * 当用户尝试访问受保护的资源但未提供有效认证时，此类用于处理认证失败的情况
 * 它实现了Spring Security的AuthenticationEntryPoint接口
 *
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // 日志记录器，用于记录认证失败的相关信息
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // 记录认证失败的错误信息
        logger.error("Responding with unauthorized error. Message - {}", authException.getMessage());

        // 设置响应的内容类型和字符编码
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        // 设置响应的状态码为未授权
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 创建一个错误的响应结果，指示认证失败，需要重新登录
        Result<Object> result = Result.error(401, "认证失败，请重新登录");

        // 使用ObjectMapper将Result对象转换为JSON格式并写入响应流
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), result);
    }
}
