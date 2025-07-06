package com.hospital.report.executor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheManager {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String CACHE_PREFIX = "sql_cache:";
    private static final String CACHE_STATS_PREFIX = "sql_cache_stats:";
    private static final String CACHE_INDEX_KEY = "sql_cache_index";
    private static final int DEFAULT_CACHE_TTL = 3600; // 1 hour in seconds
    private static final int MAX_CACHE_SIZE = 1000;

    public String generateCacheKey(String sql, Map<String, Object> parameters) {
        if (!StringUtils.hasText(sql)) {
            return null;
        }

        String normalizedSql = normalizeSql(sql);
        String paramString = parameters != null ? parameters.toString() : "";
        String combinedString = normalizedSql + "|" + paramString;
        
        return CACHE_PREFIX + DigestUtils.md5DigestAsHex(combinedString.getBytes());
    }

    public SqlExecutor.ExecutionResult getFromCache(String cacheKey) {
        if (!StringUtils.hasText(cacheKey)) {
            return null;
        }

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof SqlExecutor.ExecutionResult) {
                updateCacheStats(cacheKey, true);
                log.debug("Cache hit for key: {}", cacheKey);
                return (SqlExecutor.ExecutionResult) cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve from cache: {}", e.getMessage());
        }

        updateCacheStats(cacheKey, false);
        return null;
    }

    public void putInCache(String cacheKey, SqlExecutor.ExecutionResult result) {
        if (!StringUtils.hasText(cacheKey) || result == null || !result.isSuccess()) {
            return;
        }

        try {
            // Check if we should cache this result
            if (!shouldCacheResult(result)) {
                return;
            }

            // Check cache size limit
            if (getCacheSize() >= MAX_CACHE_SIZE) {
                evictOldestEntries(100); // Remove 100 oldest entries
            }

            // Store the result with TTL
            int ttl = calculateTtl(result);
            redisTemplate.opsForValue().set(cacheKey, result, ttl, TimeUnit.SECONDS);
            
            // Add to cache index
            redisTemplate.opsForSet().add(CACHE_INDEX_KEY, cacheKey);
            
            // Initialize cache stats for this key
            initializeCacheStats(cacheKey);
            
            log.debug("Cached result for key: {} with TTL: {}s", cacheKey, ttl);
            
        } catch (Exception e) {
            log.warn("Failed to cache result: {}", e.getMessage());
        }
    }

    public void invalidateCache(String pattern) {
        try {
            if (StringUtils.hasText(pattern)) {
                Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*" + pattern + "*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                    
                    // Remove from cache index
                    for (String key : keys) {
                        redisTemplate.opsForSet().remove(CACHE_INDEX_KEY, key);
                    }
                    
                    log.info("Invalidated {} cache entries matching pattern: {}", keys.size(), pattern);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to invalidate cache: {}", e.getMessage());
        }
    }

    public void clearCache() {
        try {
            Set<String> keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
            
            // Clear cache index
            redisTemplate.delete(CACHE_INDEX_KEY);
            
            // Clear cache stats
            Set<String> statsKeys = redisTemplate.keys(CACHE_STATS_PREFIX + "*");
            if (statsKeys != null && !statsKeys.isEmpty()) {
                redisTemplate.delete(statsKeys);
            }
            
            log.info("Cleared all cache entries");
            
        } catch (Exception e) {
            log.warn("Failed to clear cache: {}", e.getMessage());
        }
    }

    public Map<String, Object> getCacheStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Cache size
            Long cacheSize = redisTemplate.opsForSet().size(CACHE_INDEX_KEY);
            stats.put("cacheSize", cacheSize != null ? cacheSize : 0);
            stats.put("maxCacheSize", MAX_CACHE_SIZE);
            
            // Calculate hit/miss statistics
            Set<String> cacheKeys = redisTemplate.opsForSet().members(CACHE_INDEX_KEY);
            long totalHits = 0;
            long totalMisses = 0;
            long totalRequests = 0;
            
            if (cacheKeys != null) {
                for (String cacheKey : cacheKeys) {
                    Map<Object, Object> keyStats = redisTemplate.opsForHash().entries(CACHE_STATS_PREFIX + cacheKey);
                    if (keyStats != null) {
                        totalHits += getLongValue(keyStats.get("hits"));
                        totalMisses += getLongValue(keyStats.get("misses"));
                    }
                }
            }
            
            totalRequests = totalHits + totalMisses;
            double hitRate = totalRequests > 0 ? (double) totalHits / totalRequests * 100 : 0;
            
            stats.put("totalHits", totalHits);
            stats.put("totalMisses", totalMisses);
            stats.put("totalRequests", totalRequests);
            stats.put("hitRate", hitRate);
            
            // Memory usage estimation
            long estimatedMemoryUsage = cacheSize != null ? cacheSize * 1024 : 0; // Rough estimate
            stats.put("estimatedMemoryUsage", estimatedMemoryUsage);
            
        } catch (Exception e) {
            log.warn("Failed to get cache statistics: {}", e.getMessage());
        }
        
        return stats;
    }

    public List<Map<String, Object>> getCacheEntries(int limit) {
        List<Map<String, Object>> entries = new ArrayList<>();
        
        try {
            Set<String> cacheKeys = redisTemplate.opsForSet().members(CACHE_INDEX_KEY);
            if (cacheKeys != null) {
                int count = 0;
                for (String cacheKey : cacheKeys) {
                    if (count >= limit) break;
                    
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("key", cacheKey);
                    
                    // Get TTL
                    Long ttl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
                    entry.put("ttl", ttl);
                    
                    // Get stats
                    Map<Object, Object> keyStats = redisTemplate.opsForHash().entries(CACHE_STATS_PREFIX + cacheKey);
                    if (keyStats != null) {
                        entry.put("hits", getLongValue(keyStats.get("hits")));
                        entry.put("misses", getLongValue(keyStats.get("misses")));
                        entry.put("createdAt", keyStats.get("createdAt"));
                        entry.put("lastAccessed", keyStats.get("lastAccessed"));
                    }
                    
                    entries.add(entry);
                    count++;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to get cache entries: {}", e.getMessage());
        }
        
        return entries;
    }

    public void warmUpCache(List<String> commonQueries) {
        // TODO: Implement cache warm-up functionality
        log.info("Cache warm-up not yet implemented");
    }

    public void optimizeCache() {
        try {
            // Remove expired entries from index
            Set<String> cacheKeys = redisTemplate.opsForSet().members(CACHE_INDEX_KEY);
            if (cacheKeys != null) {
                List<String> expiredKeys = new ArrayList<>();
                for (String cacheKey : cacheKeys) {
                    if (!redisTemplate.hasKey(cacheKey)) {
                        expiredKeys.add(cacheKey);
                    }
                }
                
                if (!expiredKeys.isEmpty()) {
                    redisTemplate.opsForSet().remove(CACHE_INDEX_KEY, expiredKeys.toArray());
                    log.info("Removed {} expired entries from cache index", expiredKeys.size());
                }
            }
            
            // Remove cache stats for non-existent keys
            Set<String> statsKeys = redisTemplate.keys(CACHE_STATS_PREFIX + "*");
            if (statsKeys != null) {
                List<String> orphanedStatsKeys = new ArrayList<>();
                for (String statsKey : statsKeys) {
                    String cacheKey = statsKey.replace(CACHE_STATS_PREFIX, "");
                    if (!redisTemplate.hasKey(CACHE_PREFIX + cacheKey)) {
                        orphanedStatsKeys.add(statsKey);
                    }
                }
                
                if (!orphanedStatsKeys.isEmpty()) {
                    redisTemplate.delete(orphanedStatsKeys);
                    log.info("Removed {} orphaned cache stats entries", orphanedStatsKeys.size());
                }
            }
            
        } catch (Exception e) {
            log.warn("Failed to optimize cache: {}", e.getMessage());
        }
    }

    private String normalizeSql(String sql) {
        return sql.trim()
                  .toLowerCase()
                  .replaceAll("\\s+", " ")
                  .replaceAll("--.*$", "")
                  .replaceAll("/\\*.*?\\*/", "");
    }

    private boolean shouldCacheResult(SqlExecutor.ExecutionResult result) {
        // Don't cache if result is too large
        if (result.getData() instanceof List) {
            List<?> data = (List<?>) result.getData();
            if (data.size() > 10000) {
                return false;
            }
        }
        
        // Don't cache update/insert/delete operations
        String queryType = result.getQueryType();
        if (queryType != null && !queryType.equals("SELECT")) {
            return false;
        }
        
        // Don't cache if execution time was too short (likely a simple query)
        if (result.getExecutionTime() != null && result.getExecutionTime() < 100) {
            return false;
        }
        
        return true;
    }

    private int calculateTtl(SqlExecutor.ExecutionResult result) {
        // Base TTL
        int ttl = DEFAULT_CACHE_TTL;
        
        // Longer TTL for slower queries
        if (result.getExecutionTime() != null) {
            if (result.getExecutionTime() > 5000) {
                ttl = 7200; // 2 hours
            } else if (result.getExecutionTime() > 1000) {
                ttl = 3600; // 1 hour
            }
        }
        
        return ttl;
    }

    private long getCacheSize() {
        try {
            Long size = redisTemplate.opsForSet().size(CACHE_INDEX_KEY);
            return size != null ? size : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    private void evictOldestEntries(int count) {
        try {
            // This is a simplified eviction strategy
            // In a real implementation, you might want to use LRU or other algorithms
            Set<String> cacheKeys = redisTemplate.opsForSet().members(CACHE_INDEX_KEY);
            if (cacheKeys != null && cacheKeys.size() > count) {
                List<String> keysList = new ArrayList<>(cacheKeys);
                List<String> keysToEvict = keysList.subList(0, count);
                
                redisTemplate.delete(keysToEvict);
                redisTemplate.opsForSet().remove(CACHE_INDEX_KEY, keysToEvict.toArray());
                
                log.info("Evicted {} oldest cache entries", count);
            }
        } catch (Exception e) {
            log.warn("Failed to evict cache entries: {}", e.getMessage());
        }
    }

    private void updateCacheStats(String cacheKey, boolean hit) {
        try {
            String statsKey = CACHE_STATS_PREFIX + cacheKey;
            String field = hit ? "hits" : "misses";
            
            redisTemplate.opsForHash().increment(statsKey, field, 1);
            redisTemplate.opsForHash().put(statsKey, "lastAccessed", LocalDateTime.now().toString());
            
        } catch (Exception e) {
            log.warn("Failed to update cache stats: {}", e.getMessage());
        }
    }

    private void initializeCacheStats(String cacheKey) {
        try {
            String statsKey = CACHE_STATS_PREFIX + cacheKey;
            Map<String, Object> initialStats = new HashMap<>();
            initialStats.put("hits", 0);
            initialStats.put("misses", 0);
            initialStats.put("createdAt", LocalDateTime.now().toString());
            initialStats.put("lastAccessed", LocalDateTime.now().toString());
            
            redisTemplate.opsForHash().putAll(statsKey, initialStats);
            
        } catch (Exception e) {
            log.warn("Failed to initialize cache stats: {}", e.getMessage());
        }
    }

    private long getLongValue(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}