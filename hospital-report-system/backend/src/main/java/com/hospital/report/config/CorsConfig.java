package com.hospital.report.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * 跨域配置
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private List<String> allowedOriginPatterns = Arrays.asList(
        "http://localhost:*",
        "http://127.0.0.1:*"
    );

    private List<String> allowedMethods = Arrays.asList(
        "GET", "POST", "PUT", "DELETE", "OPTIONS"
    );

    private List<String> allowedHeaders = Arrays.asList(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "Accept",
        "Origin",
        "Access-Control-Request-Method",
        "Access-Control-Request-Headers"
    );

    private boolean allowCredentials = false;

    private long maxAge = 3600;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                .allowedMethods(allowedMethods.toArray(new String[0]))
                .allowedHeaders(allowedHeaders.toArray(new String[0]))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    /**
     * CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 设置允许的源
        configuration.setAllowedOriginPatterns(allowedOriginPatterns);
        
        // 设置允许的方法
        configuration.setAllowedMethods(allowedMethods);
        
        // 设置允许的头部
        configuration.setAllowedHeaders(allowedHeaders);
        
        // 设置暴露的头部
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        
        // 是否允许凭证
        configuration.setAllowCredentials(allowCredentials);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}