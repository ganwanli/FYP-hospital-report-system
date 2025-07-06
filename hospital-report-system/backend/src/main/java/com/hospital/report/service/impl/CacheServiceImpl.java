package com.hospital.report.service.impl;

import com.hospital.report.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 基于内存的缓存服务实现
 * 生产环境应该使用Redis实现
 */
@Slf4j
@Service
public class CacheServiceImpl implements CacheService {
    
    private final ObjectMapper objectMapper;
    
    // 内存缓存存储
    private final Map<String, CacheItem> cache = new ConcurrentHashMap<>();
    
    // 定时清理过期缓存
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    // 缓存项内部类
    private static class CacheItem {
        private final Object value;
        private final long expireTime;
        
        public CacheItem(Object value, long expireTime) {
            this.value = value;
            this.expireTime = expireTime;
        }
        
        public Object getValue() {
            return value;
        }
        
        public boolean isExpired() {
            return expireTime > 0 && System.currentTimeMillis() > expireTime;
        }
        
        public long getTtl() {
            if (expireTime <= 0) {
                return -1; // 永不过期
            }
            long remaining = expireTime - System.currentTimeMillis();
            return remaining > 0 ? remaining / 1000 : -2; // 已过期
        }
    }
    
    // 构造函数中启动清理任务
    public CacheServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        // 每5分钟清理一次过期缓存
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredItems, 5, 5, TimeUnit.MINUTES);
    }
    
    @Override
    public void set(String key, Object value, int expireSeconds) {
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为空");
        }
        
        try {
            long expireTime = expireSeconds > 0 ? System.currentTimeMillis() + expireSeconds * 1000L : -1;
            cache.put(key, new CacheItem(value, expireTime));
            
            log.debug("设置缓存: {} = {}, 过期时间: {}秒", key, value, expireSeconds);
        } catch (Exception e) {
            log.error("设置缓存失败: {}", key, e);
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        if (key == null) {
            return null;
        }
        
        try {
            CacheItem item = cache.get(key);
            if (item == null) {
                log.debug("缓存未找到: {}", key);
                return null;
            }
            
            if (item.isExpired()) {
                cache.remove(key);
                log.debug("缓存已过期: {}", key);
                return null;
            }
            
            Object value = item.getValue();
            
            // 类型转换
            if (value == null) {
                return null;
            }
            
            if (clazz.isAssignableFrom(value.getClass())) {
                return (T) value;
            }
            
            // 尝试JSON转换
            if (value instanceof String) {
                return objectMapper.readValue((String) value, clazz);
            } else {
                // 对象转换
                String json = objectMapper.writeValueAsString(value);
                return objectMapper.readValue(json, clazz);
            }
            
        } catch (Exception e) {
            log.error("获取缓存失败: {}", key, e);
            return null;
        }
    }
    
    @Override
    public void delete(String key) {
        if (key == null) {
            return;
        }
        
        CacheItem removed = cache.remove(key);
        if (removed != null) {
            log.debug("删除缓存: {}", key);
        }
    }
    
    @Override
    public boolean exists(String key) {
        if (key == null) {
            return false;
        }
        
        CacheItem item = cache.get(key);
        if (item == null) {
            return false;
        }
        
        if (item.isExpired()) {
            cache.remove(key);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void expire(String key, int expireSeconds) {
        if (key == null) {
            return;
        }
        
        CacheItem item = cache.get(key);
        if (item != null && !item.isExpired()) {
            long expireTime = expireSeconds > 0 ? System.currentTimeMillis() + expireSeconds * 1000L : -1;
            cache.put(key, new CacheItem(item.getValue(), expireTime));
            log.debug("更新缓存过期时间: {} = {}秒", key, expireSeconds);
        }
    }
    
    @Override
    public long ttl(String key) {
        if (key == null) {
            return -2;
        }
        
        CacheItem item = cache.get(key);
        if (item == null) {
            return -2; // 键不存在
        }
        
        return item.getTtl();
    }
    
    @Override
    public void deleteByPattern(String pattern) {
        if (pattern == null) {
            return;
        }
        
        // 简单的通配符匹配实现
        String regex = pattern.replace("*", ".*").replace("?", ".");
        
        cache.entrySet().removeIf(entry -> {
            boolean matches = entry.getKey().matches(regex);
            if (matches) {
                log.debug("按模式删除缓存: {} 匹配 {}", entry.getKey(), pattern);
            }
            return matches;
        });
    }
    
    @Override
    public long increment(String key, long delta) {
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为空");
        }
        
        CacheItem item = cache.get(key);
        long currentValue = 0;
        
        if (item != null && !item.isExpired()) {
            Object value = item.getValue();
            if (value instanceof Number) {
                currentValue = ((Number) value).longValue();
            }
        }
        
        long newValue = currentValue + delta;
        set(key, newValue, -1); // 永不过期
        
        log.debug("递增缓存计数器: {} + {} = {}", key, delta, newValue);
        return newValue;
    }
    
    @Override
    public boolean setNx(String key, Object value, int expireSeconds) {
        if (key == null) {
            throw new IllegalArgumentException("缓存键不能为空");
        }
        
        if (exists(key)) {
            return false; // 键已存在
        }
        
        set(key, value, expireSeconds);
        return true;
    }
    
    // 清理过期项
    private void cleanupExpiredItems() {
        try {
            java.util.concurrent.atomic.AtomicInteger removed = new java.util.concurrent.atomic.AtomicInteger(0);
            cache.entrySet().removeIf(entry -> {
                boolean expired = entry.getValue().isExpired();
                if (expired) {
                    removed.incrementAndGet();
                }
                return expired;
            });
            if (removed.get() > 0) {
                log.debug("清理过期缓存项: {} 个", removed.get());
            }
            log.debug("当前缓存项数量: {}", cache.size());
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        
        long totalItems = cache.size();
        long expiredItems = cache.values().stream()
            .mapToLong(item -> item.isExpired() ? 1 : 0)
            .sum();
        
        stats.put("totalItems", totalItems);
        stats.put("expiredItems", expiredItems);
        stats.put("activeItems", totalItems - expiredItems);
        
        return stats;
    }
    
    /**
     * 清空所有缓存
     */
    public void clear() {
        cache.clear();
        log.info("清空所有缓存");
    }
}