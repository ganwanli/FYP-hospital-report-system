package com.hospital.report.service;

import com.hospital.report.entity.ReportShare;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Map;
import java.util.List;

/**
 * 报表分享服务接口
 */
public interface ReportShareService {
    
    /**
     * 创建分享
     * @param reportShare 分享配置
     * @return 创建的分享
     */
    ReportShare createShare(ReportShare reportShare);
    
    /**
     * 更新分享
     * @param reportShare 分享配置
     * @return 更新后的分享
     */
    ReportShare updateShare(ReportShare reportShare);
    
    /**
     * 删除分享
     * @param shareId 分享ID
     */
    void deleteShare(Long shareId);
    
    /**
     * 根据分享码获取分享信息
     * @param shareCode 分享码
     * @return 分享信息
     */
    ReportShare getShareByCode(String shareCode);
    
    /**
     * 获取分享列表
     * @param page 分页对象
     * @param reportId 报表ID（可选）
     * @param shareType 分享类型（可选）
     * @param isActive 是否激活（可选）
     * @param createdBy 创建者ID（可选）
     * @return 分页结果
     */
    IPage<ReportShare> getShareList(Page<ReportShare> page, Long reportId, String shareType, Boolean isActive, Long createdBy);
    
    /**
     * 验证分享访问权限
     * @param shareCode 分享码
     * @param password 访问密码（可选）
     * @return 验证结果
     */
    Map<String, Object> validateShareAccess(String shareCode, String password);
    
    /**
     * 记录分享访问
     * @param shareCode 分享码
     * @param accessInfo 访问信息
     */
    void recordShareAccess(String shareCode, Map<String, Object> accessInfo);
    
    /**
     * 获取分享统计信息
     * @param shareId 分享ID
     * @return 统计信息
     */
    Map<String, Object> getShareStatistics(Long shareId);
    
    /**
     * 刷新分享码
     * @param shareId 分享ID
     * @return 新的分享码
     */
    String refreshShareCode(Long shareId);
    
    /**
     * 禁用过期分享
     */
    void disableExpiredShares();
    
    /**
     * 生成分享URL
     * @param shareCode 分享码
     * @param baseUrl 基础URL
     * @return 完整的分享URL
     */
    String generateShareUrl(String shareCode, String baseUrl);
}