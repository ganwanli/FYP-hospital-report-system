package com.hospital.report.security;

import com.hospital.report.utils.JwtTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Component
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // 日志记录器，用于记录系统日志
private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

// JwtTokenUtil工具类，用于JWT令牌的处理
@Autowired
private JwtTokenUtil jwtTokenUtil;

// UserDetailsService服务，用于用户详情的加载
@Autowired
private UserDetailsService userDetailsService;

/**
 * 执行过滤器的主要方法
 *
 * @param request HTTP请求对象，包含请求信息
 * @param response HTTP响应对象，包含响应信息
 * @param filterChain 过滤链，用于将请求传递给下一个过滤器或目标资源
 * @throws ServletException 如果请求处理过程中发生Servlet异常
 * @throws IOException 如果请求处理过程中发生IO异常
 */
@Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    // 检查是否是公开端点，如果是则跳过JWT验证
    String requestURI = request.getRequestURI();
    logger.debug("Processing request URI: {}", requestURI);
    /**
     * 暂时屏蔽，因为Spring Security已经过滤了
    * */
//    if (isPublicEndpoint(requestURI)) {
//        logger.debug("Request URI {} is a public endpoint, skipping JWT validation", requestURI);
//        filterChain.doFilter(request, response);
//        return;
//    }
//    logger.debug("Request URI {} requires JWT validation", requestURI);

    try {
        // 从请求中获取JWT令牌
        String jwt = getJwtFromRequest(request);
        logger.debug("Extracted JWT token: {}", jwt != null ? "present" : "null");

        // 验证JWT令牌是否有效
        if (StringUtils.hasText(jwt) && jwtTokenUtil.validateToken(jwt)) {
            logger.debug("JWT token is valid");
            // 从JWT令牌中获取用户名
            String username = jwtTokenUtil.getUsernameFromToken(jwt);
            logger.debug("Username from token: {}", username);

            // 加载用户详情
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            // 进一步验证JWT令牌与用户详情是否匹配
            if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                logger.debug("JWT token validation with user details successful");
                // 创建认证令牌并设置用户权限
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // 设置认证详情
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 将认证信息设置到SecurityContext中
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authentication set in SecurityContext for user: {}", username);
            } else {
                logger.debug("JWT token validation with user details failed");
            }
        } else {
            logger.debug("JWT token is null, empty, or invalid");
        }
    } catch (Exception ex) {
        // 记录设置用户认证信息时发生的异常
        logger.error("Could not set user authentication in security context", ex);
    }

    // 继续执行请求链中的下一个过滤器或目标资源
    filterChain.doFilter(request, response);
}

/**
 * 检查是否是公开端点  暂时屏蔽
 */
//private boolean isPublicEndpoint(String requestURI) {
//    String[] publicEndpoints = {
//        "/api/auth/",
//        "/api/datasource/active",
//        "/api/datasource/list",
//        "/api/datasource",  // 数据源CRUD操作
//        "/api/datasource/",
//        "/api/datasource/test-simple",  // 数据源测试连接
//        //"/api/system/dict/items/",
//        "/api/system/depts",
//        "/api/sql-templates",
//        "/api/sql-templates/",
//        "/api/sql-execution",  // SQL执行相关接口
//        "/api/sql-execution/",
//        "/api/swagger-ui/",
//        "/api/v3/api-docs/",
//        "/api/actuator/"
//    };
//
//    for (String endpoint : publicEndpoints) {
//        if (requestURI.equals(endpoint) || requestURI.startsWith(endpoint)) {
//            return true;
//        }
//    }
//
//    // 特殊处理SQL模板相关的所有端点
//    if (requestURI.contains("/sql-templates")) {
//        return true;
//    }
//
//    // 特殊处理SQL执行相关的所有端点
//    if (requestURI.contains("/sql-execution")) {
//        return true;
//    }
//
//    return false;
//}


    /**
     * 从请求中获取JWT token
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        return jwtTokenUtil.getTokenFromHeader(bearerToken);
    }
}