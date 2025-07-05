package com.hospital.report.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置类
 * 
 * @author Hospital Report System
 * @since 2024-01-01
 */
@Configuration
@MapperScan("com.hospital.report.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis Plus 拦截器配置
     * 
     * @return MybatisPlusInterceptor
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 分页插件
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        paginationInterceptor.setMaxLimit(1000L); // 单页分页条数限制
        paginationInterceptor.setOverflow(false); // 溢出总页数后是否进行处理
        paginationInterceptor.setOptimizeJoin(true); // 生成countSql优化掉join
        interceptor.addInnerInterceptor(paginationInterceptor);
        
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        
        // 防止全表更新与删除插件
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        
        return interceptor;
    }

    /**
     * 自动填充处理器
     */
    @Component
    public static class MyMetaObjectHandler implements MetaObjectHandler {

        /**
         * 插入时的填充策略
         */
        @Override
        public void insertFill(MetaObject metaObject) {
            // 创建时间
            this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
            
            // 更新时间
            this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            
            // 删除标志
            this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
            this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
            
            // 状态
            this.strictInsertFill(metaObject, "status", Integer.class, 1);
            
            // 版本号
            this.strictInsertFill(metaObject, "version", Integer.class, 1);
            
            // TODO: 从SecurityContext中获取当前用户ID
            // Long userId = getCurrentUserId();
            // if (userId != null) {
            //     this.strictInsertFill(metaObject, "createdBy", Long.class, userId);
            //     this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            // }
        }

        /**
         * 更新时的填充策略
         */
        @Override
        public void updateFill(MetaObject metaObject) {
            // 更新时间
            this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
            this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            
            // TODO: 从SecurityContext中获取当前用户ID
            // Long userId = getCurrentUserId();
            // if (userId != null) {
            //     this.strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
            //     this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
            // }
        }

        /**
         * 获取当前用户ID
         * TODO: 实现获取当前登录用户ID的逻辑
         */
        private Long getCurrentUserId() {
            // SecurityContext context = SecurityContextHolder.getContext();
            // Authentication authentication = context.getAuthentication();
            // if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            //     // 返回当前用户ID
            // }
            return null;
        }
    }
}