package com.hospital.report.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT工具类
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Component
public class JwtTokenUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 从token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从token中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从token中获取指定claim
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 从token中获取所有claims
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 检查token是否过期
     */
    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 为用户生成token
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 为用户生成token（带额外claims）
     */
    public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * 生成refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createRefreshToken(claims, userDetails.getUsername());
    }

    /**
     * 创建token
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 创建refresh token
     */
    private String createRefreshToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证token
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            logger.error("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 验证token是否有效
     */
    public Boolean validateToken(String token) {
            // 使用JWT进行身份验证，检查token的有效性
        try {
            // 构建JWT解析器，并设置签名密钥
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    // 解析并验证JWT token
                    .parseClaimsJws(token);
            // 如果token有效，返回true
            return true;
        } catch (SecurityException e) {
            // 捕获安全性异常，表明JWT签名无效
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // 捕获格式错误异常，表明JWT token格式不正确
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            // 捕获过期异常，表明JWT token已过期
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // 捕获不支持异常，表明JWT token类型不支持
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // 捕获非法参数异常，表明JWT claims字符串为空
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        // 如果token无效，返回false
        return false;

    }

    /**
     * 从请求头中获取token
     */
    public String getTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith(prefix)) {
            return authHeader.substring(prefix.length());
        }
        return null;
    }

    /**
     * 获取签名密钥
     */
    private SecretKey getSigningKey() {
        if (secret == null || secret.isEmpty()) {
            logger.error("JWT secret is not configured!");
            throw new IllegalStateException("JWT secret is not configured");
        }

        // 确保密钥长度足够
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            logger.warn("JWT secret is too short ({} bytes), should be at least 32 bytes", keyBytes.length);
        }

        try {
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            logger.error("Failed to create signing key: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + expiration));
            
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Token刷新失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取token中的用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            return claims.get("userId", Long.class);
        } catch (Exception e) {
            logger.error("从token中获取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取token剩余有效时间
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * 获取token过期时间
     */
    public long getExpirationTime() {
        return expiration;
    }
    
    /**
     * 验证refresh token
     */
    public Boolean validateRefreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            String type = claims.get("type", String.class);
            return "refresh".equals(type) && !isTokenExpired(token);
        } catch (Exception e) {
            logger.error("Refresh token验证失败: {}", e.getMessage());
            return false;
        }
    }
}