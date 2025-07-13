package com.hospital.report.config;

import com.hospital.report.security.JwtAuthenticationEntryPoint;
import com.hospital.report.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

/**
 * Spring Security配置类
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;

    @PostConstruct
    public void init() {
        log.info("SecurityConfig 初始化完成");
        log.info("jwtAuthenticationFilter: {}", jwtAuthenticationFilter != null ? "已注入" : "未注入");
        log.info("jwtAuthenticationEntryPoint: {}", jwtAuthenticationEntryPoint != null ? "已注入" : "未注入");
        log.info("corsConfigurationSource: {}", corsConfigurationSource != null ? "已注入" : "未注入");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("创建 PasswordEncoder Bean");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        log.info("创建 AuthenticationManager Bean");
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public HttpFirewall allowUrlEncodedHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        // 允许百分号编码
        firewall.setAllowUrlEncodedPercent(true);
        // 允许分号
        firewall.setAllowSemicolon(true);
        // 允许斜杠
        firewall.setAllowBackSlash(true);
        // 允许URL编码的斜杠
        firewall.setAllowUrlEncodedSlash(true);
        // 允许URL编码的换行符
        firewall.setAllowUrlEncodedLineFeed(true);
        firewall.setAllowUrlEncodedCarriageReturn(true);

        // 使用 Predicate 而不是 List
        // 允许所有头名称和值（包括可能包含换行符的值）
        firewall.setAllowedHeaderNames(name -> true);
        firewall.setAllowedHeaderValues(value -> true);

        log.info("配置自定义 HttpFirewall，放宽 URL 安全限制");
        return firewall;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(allowUrlEncodedHttpFirewall());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("配置 SecurityFilterChain");
        
        http
            .csrf(csrf -> {
                log.info("禁用 CSRF 保护");
                csrf.disable();
            })
            .cors(cors -> {
                log.info("配置 CORS");
                cors.configurationSource(corsConfigurationSource);
            })
            .exceptionHandling(exception -> {
                log.info("配置异常处理");
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint);
            })
            .sessionManagement(session -> {
                log.info("配置会话管理");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(authorize -> {
                log.info("配置请求授权");
                authorize
                    .requestMatchers("/auth/**").permitAll()  // 移除 /api 前缀，因为context path已经是/api
                    .requestMatchers("/datasource/active").permitAll()
                    .requestMatchers("/datasource/list").permitAll()
                    .requestMatchers("/datasource", "/datasource/**").permitAll()  // 允许数据源CRUD操作公开访问
                    .requestMatchers("/datasource/test-simple").permitAll()  // 允许数据源测试连接公开访问
                    .requestMatchers("/system/dict/items/**").permitAll()
                    .requestMatchers("/system/depts", "/system/depts/**").permitAll()  // 允许部门接口公开访问
                    .requestMatchers("/sql-templates", "/sql-templates/**").permitAll()  // 允许SQL模板相关接口公开访问
                    .requestMatchers(HttpMethod.GET, "/sql-templates").permitAll()  // 明确允许GET请求
                    .requestMatchers(HttpMethod.GET, "/sql-templates/**").permitAll()  // 明确允许GET请求
                    .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain 配置完成");
        return http.build();
    }
}