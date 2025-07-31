package com.hospital.report.config;

import com.hospital.report.security.JwtAuthenticationEntryPoint;
import com.hospital.report.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


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
@EnableConfigurationProperties(IgnoreUrlsConfig.class)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final CorsConfigurationSource corsConfigurationSource;
    private final UserDetailsService userDetailsService;


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
    public IgnoreUrlsConfig ignoreUrlsConfig() {
        return new IgnoreUrlsConfig();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        log.info("创建 DaoAuthenticationProvider Bean");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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

//    @Bean
//    public JwtAuthenticationFilter jwtAuthenticationFilter() {
//        return new JwtAuthenticationFilter();
//    }

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
                exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                    log.error("Access denied for request: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
                    if (!response.isCommitted()) {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"error\":\"Access Denied\",\"message\":\"" + accessDeniedException.getMessage() + "\"}");
                    }
                });
            })
            .sessionManagement(session -> {
                log.info("配置会话管理");
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
            })
            .authorizeHttpRequests(authorize -> {
                log.info("配置请求授权");
                
                // 配置公开访问的端点 - 注意：由于context-path是/api，Spring Security看到的路径不包含/api前缀
                authorize.requestMatchers("/auth/**").permitAll();
                authorize.requestMatchers("/swagger-ui/**").permitAll();
                authorize.requestMatchers("/v3/api-docs/**").permitAll();
                authorize.requestMatchers("/actuator/**").permitAll();
                authorize.requestMatchers("/datasource/**").permitAll();
                authorize.requestMatchers("/sql-execution/**").permitAll();
                authorize.requestMatchers("/sql-templates/**").permitAll();
                authorize.requestMatchers("/ai-assistant/**").permitAll(); // 修复：移除/api前缀
                authorize.requestMatchers("/system/dict/**").permitAll();
                
                log.info("AI助手端点已添加到允许列表: /ai-assistant/**");
                
                // 继续使用配置文件中的其他忽略URL（如果存在）
                String[] urls = ignoreUrlsConfig().getUrls();
                log.info("忽略URL配置: {}", urls != null ? java.util.Arrays.toString(urls) : "null");
                if (urls != null) {
                    for (String url : urls) {
                        log.info("添加公开访问URL: {}", url);
                        // 由于context-path是/api，Spring Security处理的路径不包含/api前缀
                        authorize.requestMatchers(new AntPathRequestMatcher(url)).permitAll();
                    }
                }
                
                authorize.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain 配置完成");
        return http.build();
    }
}