package com.hospital.report.service;

import com.hospital.report.dto.ReportAuditDTO;
import org.springframework.security.core.Authentication;

import java.util.List;

/**
 * 报表审核服务接口
 */
public interface ReportAuditService {

    /**
     * 审核单个报表
     * @param reportId 报表ID
     * @param request 审核请求
     * @param authentication 认证信息
     * @return 审核结果
     */
    ReportAuditDTO.AuditResult auditReport(Long reportId, 
                                          ReportAuditDTO.AuditRequest request, 
                                          Authentication authentication);

    /**
     * 批量审核报表
     * @param request 批量审核请求
     * @param authentication 认证信息
     * @return 批量审核结果
     */
    ReportAuditDTO.BatchAuditResult batchAuditReports(ReportAuditDTO.BatchAuditRequest request, 
                                                     Authentication authentication);

    /**
     * 发布报表
     * @param reportId 报表ID
     * @param authentication 认证信息
     * @return 发布结果
     */
    ReportAuditDTO.PublishResult publishReport(Long reportId, Authentication authentication);

    /**
     * 取消发布报表
     * @param reportId 报表ID
     * @param authentication 认证信息
     * @return 取消发布结果
     */
    ReportAuditDTO.UnpublishResult unpublishReport(Long reportId, Authentication authentication);

    /**
     * 重新提交审核
     * @param reportId 报表ID
     * @param authentication 认证信息
     */
    void resubmitForAudit(Long reportId, Authentication authentication);

    /**
     * 获取审核统计
     * @return 审核统计信息
     */
    ReportAuditDTO.AuditStatistics getAuditStatistics();

    /**
     * 获取发布统计
     * @return 发布统计信息
     */
    ReportAuditDTO.PublishStatistics getPublishStatistics();

    /**
     * 获取审核历史
     * @param reportId 报表ID
     * @param page 页码
     * @param size 页大小
     * @return 审核历史列表
     */
    List<ReportAuditDTO.AuditLog> getAuditHistory(Long reportId, int page, int size);
}
