package com.hospital.report.config;

import com.hospital.report.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;


@Slf4j
@Configuration
public class UserDetailsServiceConfig {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Bean
    public UserDetailsService userDetailsService() {
        log.info("创建 UserDetailsService Bean");
        return username -> {
            // 这里返回 null 只是临时的，你应该实现真正的用户查询逻辑
            // 例如：从数据库中查询用户信息
            return userServiceImpl.loadUserByUsername( username);
        };
    }
}
