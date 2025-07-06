package com.hospital.report.service.impl;

import com.hospital.report.entity.ReportShare;
import com.hospital.report.service.ReportShareService;
import com.hospital.report.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportShareServiceImpl implements ReportShareService {
    
    private final CacheService cacheService;
    
    private static final String SHARE_CACHE_PREFIX = "share:";
    private static final String ACCESS_CACHE_PREFIX = "share_access:";
    private static final int SHARE_CACHE_DURATION = 3600; // 1小时
    
    // 模拟数据存储
    private final Map<Long, ReportShare> shareStorage = new HashMap<>();
    private final Map<String, ReportShare> shareCodeMap = new HashMap<>();
    private Long shareIdSequence = 1L;
    
    @Override
    public ReportShare createShare(ReportShare reportShare) {
        try {
            // 设置ID和分享码
            reportShare.setShareId(shareIdSequence++);
            reportShare.setShareCode(generateUniqueShareCode());
            reportShare.setCurrentAccessCount(0);
            reportShare.setCreatedTime(LocalDateTime.now());
            reportShare.setUpdatedTime(LocalDateTime.now());
            
            if (reportShare.getIsActive() == null) {
                reportShare.setIsActive(true);
            }
            
            // 保存到存储
            shareStorage.put(reportShare.getShareId(), reportShare);
            shareCodeMap.put(reportShare.getShareCode(), reportShare);
            
            // 缓存分享信息
            String cacheKey = SHARE_CACHE_PREFIX + reportShare.getShareCode();
            cacheService.set(cacheKey, reportShare, SHARE_CACHE_DURATION);
            
            log.info("创建报表分享成功: shareId={}, shareCode={}", reportShare.getShareId(), reportShare.getShareCode());
            return reportShare;
            
        } catch (Exception e) {
            log.error("创建报表分享失败", e);
            throw new RuntimeException("创建分享失败: " + e.getMessage());
        }
    }
    
    @Override
    public ReportShare updateShare(ReportShare reportShare) {
        try {
            ReportShare existing = shareStorage.get(reportShare.getShareId());
            if (existing == null) {
                throw new RuntimeException("分享不存在");
            }
            
            // 更新字段
            if (reportShare.getShareTitle() != null) {
                existing.setShareTitle(reportShare.getShareTitle());
            }
            if (reportShare.getShareDescription() != null) {
                existing.setShareDescription(reportShare.getShareDescription());
            }
            if (reportShare.getShareType() != null) {
                existing.setShareType(reportShare.getShareType());
            }
            if (reportShare.getAccessPassword() != null) {
                existing.setAccessPassword(reportShare.getAccessPassword());
            }
            if (reportShare.getExpireTime() != null) {
                existing.setExpireTime(reportShare.getExpireTime());
            }
            if (reportShare.getMaxAccessCount() != null) {
                existing.setMaxAccessCount(reportShare.getMaxAccessCount());
            }
            if (reportShare.getAllowExport() != null) {
                existing.setAllowExport(reportShare.getAllowExport());
            }
            if (reportShare.getAllowedFormats() != null) {
                existing.setAllowedFormats(reportShare.getAllowedFormats());
            }
            if (reportShare.getParametersConfig() != null) {
                existing.setParametersConfig(reportShare.getParametersConfig());
            }
            if (reportShare.getPermissionsConfig() != null) {
                existing.setPermissionsConfig(reportShare.getPermissionsConfig());
            }
            if (reportShare.getIsActive() != null) {
                existing.setIsActive(reportShare.getIsActive());
            }
            
            existing.setUpdatedBy(reportShare.getUpdatedBy());
            existing.setUpdatedTime(LocalDateTime.now());
            
            // 更新缓存
            String cacheKey = SHARE_CACHE_PREFIX + existing.getShareCode();
            cacheService.set(cacheKey, existing, SHARE_CACHE_DURATION);
            
            log.info("更新报表分享成功: shareId={}", existing.getShareId());
            return existing;
            
        } catch (Exception e) {
            log.error("更新报表分享失败", e);
            throw new RuntimeException("更新分享失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteShare(Long shareId) {
        try {
            ReportShare existing = shareStorage.get(shareId);
            if (existing == null) {
                throw new RuntimeException("分享不存在");
            }
            
            // 从存储中删除
            shareStorage.remove(shareId);
            shareCodeMap.remove(existing.getShareCode());
            
            // 清理缓存
            String cacheKey = SHARE_CACHE_PREFIX + existing.getShareCode();
            cacheService.delete(cacheKey);
            
            log.info("删除报表分享成功: shareId={}", shareId);
            
        } catch (Exception e) {
            log.error("删除报表分享失败", e);
            throw new RuntimeException("删除分享失败: " + e.getMessage());
        }
    }
    
    @Override
    public ReportShare getShareByCode(String shareCode) {
        try {
            // 先从缓存获取
            String cacheKey = SHARE_CACHE_PREFIX + shareCode;
            ReportShare cached = cacheService.get(cacheKey, ReportShare.class);
            if (cached != null) {
                return cached;
            }
            
            // 从存储获取
            ReportShare share = shareCodeMap.get(shareCode);
            if (share != null) {
                // 缓存结果
                cacheService.set(cacheKey, share, SHARE_CACHE_DURATION);
            }
            
            return share;
            
        } catch (Exception e) {
            log.error("获取分享信息失败: shareCode={}", shareCode, e);
            return null;
        }
    }
    
    @Override
    public IPage<ReportShare> getShareList(Page<ReportShare> page, Long reportId, String shareType, Boolean isActive, Long createdBy) {
        try {
            List<ReportShare> allShares = new ArrayList<>(shareStorage.values());
            
            // 应用过滤条件
            List<ReportShare> filteredShares = allShares.stream()
                .filter(share -> reportId == null || Objects.equals(share.getReportId(), reportId))
                .filter(share -> shareType == null || Objects.equals(share.getShareType(), shareType))
                .filter(share -> isActive == null || Objects.equals(share.getIsActive(), isActive))
                .filter(share -> createdBy == null || Objects.equals(share.getCreatedBy(), createdBy))
                .sorted((s1, s2) -> s2.getCreatedTime().compareTo(s1.getCreatedTime()))
                .toList();
            
            // 模拟分页
            int start = (int) ((page.getCurrent() - 1) * page.getSize());
            int end = Math.min(start + (int) page.getSize(), filteredShares.size());
            
            List<ReportShare> pageData = filteredShares.subList(start, end);
            
            // 创建分页结果
            Page<ReportShare> result = new Page<>(page.getCurrent(), page.getSize());
            result.setRecords(pageData);
            result.setTotal(filteredShares.size());
            
            return result;
            
        } catch (Exception e) {
            log.error("获取分享列表失败", e);
            throw new RuntimeException("获取分享列表失败: " + e.getMessage());
        }
    }
    
    @Override
    public Map<String, Object> validateShareAccess(String shareCode, String password) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ReportShare share = getShareByCode(shareCode);
            
            if (share == null) {
                result.put("valid", false);
                result.put("message", "分享不存在");
                return result;
            }
            
            if (!share.getIsActive()) {
                result.put("valid", false);
                result.put("message", "分享已禁用");
                return result;
            }
            
            // 检查过期时间
            if (share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime())) {
                result.put("valid", false);
                result.put("message", "分享已过期");
                return result;
            }
            
            // 检查访问次数限制
            if (share.getMaxAccessCount() != null && 
                share.getCurrentAccessCount() >= share.getMaxAccessCount()) {
                result.put("valid", false);
                result.put("message", "访问次数已达上限");
                return result;
            }
            
            // 检查密码
            if ("PASSWORD".equals(share.getShareType())) {
                if (password == null || !password.equals(share.getAccessPassword())) {
                    result.put("valid", false);
                    result.put("message", "访问密码错误");
                    result.put("requirePassword", true);
                    return result;
                }
            }
            
            result.put("valid", true);
            result.put("share", share);
            return result;
            
        } catch (Exception e) {
            log.error("验证分享访问权限失败: shareCode={}", shareCode, e);
            result.put("valid", false);
            result.put("message", "验证失败: " + e.getMessage());
            return result;
        }
    }
    
    @Override
    public void recordShareAccess(String shareCode, Map<String, Object> accessInfo) {
        try {
            ReportShare share = getShareByCode(shareCode);
            if (share == null) {
                return;
            }
            
            // 增加访问计数
            share.setCurrentAccessCount(share.getCurrentAccessCount() + 1);
            shareStorage.put(share.getShareId(), share);
            
            // 记录访问日志到缓存
            String accessKey = ACCESS_CACHE_PREFIX + shareCode + ":" + System.currentTimeMillis();
            cacheService.set(accessKey, accessInfo, 86400); // 缓存24小时
            
            // 更新缓存中的分享信息
            String cacheKey = SHARE_CACHE_PREFIX + shareCode;
            cacheService.set(cacheKey, share, SHARE_CACHE_DURATION);
            
            log.info("记录分享访问: shareCode={}, accessCount={}", shareCode, share.getCurrentAccessCount());
            
        } catch (Exception e) {
            log.error("记录分享访问失败: shareCode={}", shareCode, e);
        }
    }
    
    @Override
    public Map<String, Object> getShareStatistics(Long shareId) {
        try {
            ReportShare share = shareStorage.get(shareId);
            if (share == null) {
                throw new RuntimeException("分享不存在");
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("shareId", shareId);
            stats.put("shareCode", share.getShareCode());
            stats.put("totalAccess", share.getCurrentAccessCount());
            stats.put("maxAccess", share.getMaxAccessCount());
            stats.put("remainingAccess", share.getMaxAccessCount() != null ? 
                Math.max(0, share.getMaxAccessCount() - share.getCurrentAccessCount()) : -1);
            stats.put("expireTime", share.getExpireTime());
            stats.put("isActive", share.getIsActive());
            stats.put("shareType", share.getShareType());
            
            // 计算状态
            String status = "ACTIVE";
            if (!share.getIsActive()) {
                status = "DISABLED";
            } else if (share.getExpireTime() != null && LocalDateTime.now().isAfter(share.getExpireTime())) {
                status = "EXPIRED";
            } else if (share.getMaxAccessCount() != null && 
                       share.getCurrentAccessCount() >= share.getMaxAccessCount()) {
                status = "LIMIT_REACHED";
            }
            stats.put("status", status);
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取分享统计信息失败: shareId={}", shareId, e);
            throw new RuntimeException("获取统计信息失败: " + e.getMessage());
        }
    }
    
    @Override
    public String refreshShareCode(Long shareId) {
        try {
            ReportShare share = shareStorage.get(shareId);
            if (share == null) {
                throw new RuntimeException("分享不存在");
            }
            
            // 移除旧的分享码映射
            shareCodeMap.remove(share.getShareCode());
            
            // 清理旧缓存
            String oldCacheKey = SHARE_CACHE_PREFIX + share.getShareCode();
            cacheService.delete(oldCacheKey);
            
            // 生成新的分享码
            String newShareCode = generateUniqueShareCode();
            share.setShareCode(newShareCode);
            share.setUpdatedTime(LocalDateTime.now());
            
            // 更新映射
            shareCodeMap.put(newShareCode, share);
            
            // 缓存新的分享信息
            String newCacheKey = SHARE_CACHE_PREFIX + newShareCode;
            cacheService.set(newCacheKey, share, SHARE_CACHE_DURATION);
            
            log.info("刷新分享码成功: shareId={}, newShareCode={}", shareId, newShareCode);
            return newShareCode;
            
        } catch (Exception e) {
            log.error("刷新分享码失败: shareId={}", shareId, e);
            throw new RuntimeException("刷新分享码失败: " + e.getMessage());
        }
    }
    
    @Override
    public void disableExpiredShares() {
        try {
            LocalDateTime now = LocalDateTime.now();
            int disabledCount = 0;
            
            for (ReportShare share : shareStorage.values()) {
                if (share.getIsActive() && share.getExpireTime() != null && now.isAfter(share.getExpireTime())) {
                    share.setIsActive(false);
                    share.setUpdatedTime(now);
                    
                    // 更新缓存
                    String cacheKey = SHARE_CACHE_PREFIX + share.getShareCode();
                    cacheService.set(cacheKey, share, SHARE_CACHE_DURATION);
                    
                    disabledCount++;
                }
            }
            
            if (disabledCount > 0) {
                log.info("禁用过期分享: {} 个", disabledCount);
            }
            
        } catch (Exception e) {
            log.error("禁用过期分享失败", e);
        }
    }
    
    @Override
    public String generateShareUrl(String shareCode, String baseUrl) {
        try {
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            
            return baseUrl + "/share/" + shareCode;
            
        } catch (Exception e) {
            log.error("生成分享URL失败: shareCode={}", shareCode, e);
            throw new RuntimeException("生成分享URL失败: " + e.getMessage());
        }
    }
    
    // 私有方法
    
    private String generateUniqueShareCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        
        do {
            code.setLength(0);
            for (int i = 0; i < 8; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (shareCodeMap.containsKey(code.toString()));
        
        return code.toString();
    }
}