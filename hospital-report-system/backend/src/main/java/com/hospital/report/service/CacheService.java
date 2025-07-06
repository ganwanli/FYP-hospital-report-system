package com.hospital.report.service;

/**
 * 缓存服务接口
 */
public interface CacheService {
    
    /**
     * 设置缓存
     * @param key 键
     * @param value 值
     * @param expireSeconds 过期时间（秒）
     */
    void set(String key, Object value, int expireSeconds);
    
    /**
     * 获取缓存
     * @param key 键
     * @param clazz 值类型
     * @return 缓存值
     */
    <T> T get(String key, Class<T> clazz);
    
    /**
     * 删除缓存
     * @param key 键
     */
    void delete(String key);
    
    /**
     * 检查键是否存在
     * @param key 键
     * @return 是否存在
     */
    boolean exists(String key);
    
    /**
     * 设置过期时间
     * @param key 键
     * @param expireSeconds 过期时间（秒）
     */
    void expire(String key, int expireSeconds);
    
    /**
     * 获取剩余过期时间
     * @param key 键
     * @return 剩余秒数，-1表示永不过期，-2表示键不存在
     */
    long ttl(String key);
    
    /**
     * 批量删除缓存
     * @param pattern 键模式（支持通配符）
     */
    void deleteByPattern(String pattern);
    
    /**
     * 增加计数器
     * @param key 键
     * @param delta 增量
     * @return 增加后的值
     */
    long increment(String key, long delta);
    
    /**
     * 设置缓存（如果不存在）
     * @param key 键
     * @param value 值
     * @param expireSeconds 过期时间（秒）
     * @return 是否设置成功
     */
    boolean setNx(String key, Object value, int expireSeconds);
}