package com.hospital.report.service.impl;

import com.hospital.report.dto.ReportAuditDTO;
import com.hospital.report.entity.ReportAuditLog;
import com.hospital.report.entity.ReportConfig;
import com.hospital.report.exception.BusinessException;
import com.hospital.report.mapper.ReportAuditLogMapper;
import com.hospital.report.mapper.ReportConfigMapper;
import com.hospital.report.service.ReportAuditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 报表审核服务实现类
 */
@Slf4j
@Service
@Transactional
public class ReportAuditServiceImpl implements ReportAuditService {

    @Autowired
    private ReportConfigMapper reportConfigMapper;

    @Autowired
    private ReportAuditLogMapper auditLogMapper;

    @Override
    public ReportAuditDTO.AuditResult auditReport(Long reportId, 
                                                 ReportAuditDTO.AuditRequest request, 
                                                 Authentication authentication) {
        log.info("开始审核报表，reportId: {}, decision: {}", reportId, request.getDecision());

        // 1. 检查报表是否存在
        ReportConfig report = reportConfigMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报表不存在: " + reportId);
        }

        // 2. 检查报表状态是否可以审核
        if (report.getApprovalStatus() != 2) { // 2-待审核
            throw new BusinessException("报表状态不允许审核，当前状态: " + report.getApprovalStatus());
        }

        // 3. 获取当前用户信息
        String auditorName = getCurrentUserName(authentication);
        Long auditorId = getCurrentUserId(authentication);

        // 4. 记录审核日志
        ReportAuditLog auditLog = new ReportAuditLog()
                .setReportId(reportId)
                .setAuditorId(auditorId)
                .setAuditorName(auditorName)
                .setAuditDecision(request.getDecision())
                .setAuditComment(request.getComment())
                .setOldStatus(report.getApprovalStatus())
                .setNewStatus(request.getNewStatus())
                .setAuditTime(request.getAuditTime());

        auditLogMapper.insert(auditLog);

        // 5. 更新报表状态
        report.setApprovalStatus(request.getNewStatus());
        report.setAuditTime(request.getAuditTime());
        report.setAuditorId(auditorId);
        report.setAuditComment(request.getComment());
        report.setUpdatedTime(LocalDateTime.now());

        reportConfigMapper.updateById(report);

        log.info("报表审核完成，reportId: {}, 新状态: {}", reportId, request.getNewStatus());

        return ReportAuditDTO.AuditResult.builder()
                .reportId(reportId)
                .reportName(report.getReportName())
                .oldStatus(auditLog.getOldStatus())
                .newStatus(request.getNewStatus())
                .auditTime(request.getAuditTime())
                .auditorName(auditorName)
                .auditComment(request.getComment())
                .build();
    }

    @Override
    public ReportAuditDTO.BatchAuditResult batchAuditReports(ReportAuditDTO.BatchAuditRequest request, 
                                                           Authentication authentication) {
        log.info("开始批量审核报表，数量: {}, decision: {}", request.getReportIds().size(), request.getDecision());

        List<ReportAuditDTO.AuditResult> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        for (Long reportId : request.getReportIds()) {
            try {
                ReportAuditDTO.AuditRequest auditRequest = ReportAuditDTO.AuditRequest.builder()
                        .decision(request.getDecision())
                        .comment(request.getComment())
                        .auditTime(request.getAuditTime())
                        .newStatus(request.getNewStatus())
                        .build();

                ReportAuditDTO.AuditResult result = auditReport(reportId, auditRequest, authentication);
                results.add(result);
                successCount++;
            } catch (Exception e) {
                log.error("批量审核报表失败，reportId: {}, error: {}", reportId, e.getMessage());
                errors.add("报表ID " + reportId + ": " + e.getMessage());
            }
        }

        log.info("批量审核完成，成功: {}, 失败: {}", successCount, errors.size());

        return ReportAuditDTO.BatchAuditResult.builder()
                .totalCount(request.getReportIds().size())
                .successCount(successCount)
                .failureCount(errors.size())
                .errors(errors)
                .results(results)
                .build();
    }

    @Override
    public ReportAuditDTO.PublishResult publishReport(Long reportId, Authentication authentication) {
        log.info("开始发布报表，reportId: {}", reportId);

        // 1. 检查报表是否存在
        ReportConfig report = reportConfigMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报表不存在: " + reportId);
        }

        // 2. 检查报表状态是否为已审核通过
        if (report.getApprovalStatus() != 3) { // 3-审核通过
            throw new BusinessException("报表必须审核通过后才能发布，当前状态: " + report.getApprovalStatus());
        }

        // 3. 检查是否已经发布
        if (report.getIsPublished() != null && report.getIsPublished() == 1) {
            throw new BusinessException("报表已经发布");
        }

        // 4. 更新发布状态
        String publisherName = getCurrentUserName(authentication);
        Long publisherId = getCurrentUserId(authentication);
        LocalDateTime publishTime = LocalDateTime.now();

        report.setIsPublished(1);
        report.setPublishTime(publishTime);
        report.setPublisherId(publisherId);
        report.setUpdatedTime(LocalDateTime.now());

        reportConfigMapper.updateById(report);

        log.info("报表发布成功，reportId: {}", reportId);

        return ReportAuditDTO.PublishResult.builder()
                .reportId(reportId)
                .reportName(report.getReportName())
                .publishTime(publishTime)
                .publisherName(publisherName)
                .build();
    }

    @Override
    public ReportAuditDTO.UnpublishResult unpublishReport(Long reportId, Authentication authentication) {
        log.info("开始取消发布报表，reportId: {}", reportId);

        // 1. 检查报表是否存在
        ReportConfig report = reportConfigMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报表不存在: " + reportId);
        }

        // 2. 检查是否已发布
        if (report.getIsPublished() == null || report.getIsPublished() != 1) {
            throw new BusinessException("报表未发布");
        }

        // 3. 更新发布状态
        LocalDateTime unpublishTime = LocalDateTime.now();

        report.setIsPublished(0);
        report.setUnpublishTime(unpublishTime);
        report.setUpdatedTime(LocalDateTime.now());

        reportConfigMapper.updateById(report);

        log.info("报表取消发布成功，reportId: {}", reportId);

        return ReportAuditDTO.UnpublishResult.builder()
                .reportId(reportId)
                .reportName(report.getReportName())
                .unpublishTime(unpublishTime)
                .build();
    }

    @Override
    public void resubmitForAudit(Long reportId, Authentication authentication) {
        log.info("开始重新提交审核，reportId: {}", reportId);

        // 1. 检查报表是否存在
        ReportConfig report = reportConfigMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException("报表不存在: " + reportId);
        }

        // 2. 检查是否是报表创建者
        Long currentUserId = getCurrentUserId(authentication);
        if (report.getCreatedBy() == null || !report.getCreatedBy().equals(currentUserId)) {
            throw new BusinessException("只能重新提交自己创建的报表");
        }

        // 3. 检查报表状态是否允许重新提交
        if (report.getApprovalStatus() != 4) { // 4-审核拒绝
            throw new BusinessException("只有被拒绝的报表才能重新提交审核");
        }

        // 4. 更新报表状态为待审核
        report.setApprovalStatus(2); // 2-待审核
        report.setSubmitTime(LocalDateTime.now());
        report.setUpdatedTime(LocalDateTime.now());
        // 清除之前的审核信息
        report.setAuditTime(null);
        report.setAuditorId(null);
        report.setAuditComment(null);

        reportConfigMapper.updateById(report);

        log.info("报表重新提交审核成功，reportId: {}", reportId);
    }

    private String getCurrentUserName(Authentication authentication) {
        // 从认证信息中获取用户名
        return authentication.getName();
    }

    @Override
    public ReportAuditDTO.AuditStatistics getAuditStatistics() {
        log.info("获取审核统计信息");

        // 查询所有报表的统计数据
        List<ReportConfig> allReports = reportConfigMapper.selectList(null);

        int totalReports = 0;
        int pendingCount = 0;
        int approvedCount = 0;
        int rejectedCount = 0;
        int publishedCount = 0;
        int approvedUnpublishedCount = 0;

        LocalDateTime lastAuditTime = null;

        for (ReportConfig report : allReports) {
            if (report.getApprovalStatus() != null && report.getApprovalStatus() >= 2) {
                totalReports++;

                switch (report.getApprovalStatus()) {
                    case 2: // PENDING_AUDIT
                        pendingCount++;
                        break;
                    case 3: // APPROVED
                        approvedCount++;
                        if (report.getIsPublished() != null && report.getIsPublished() == 1) {
                            publishedCount++;
                        } else {
                            approvedUnpublishedCount++;
                        }
                        break;
                    case 4: // REJECTED
                        rejectedCount++;
                        break;
                }

                // 更新最后审核时间
                if (report.getAuditTime() != null) {
                    if (lastAuditTime == null || report.getAuditTime().isAfter(lastAuditTime)) {
                        lastAuditTime = report.getAuditTime();
                    }
                }
            }
        }

        // 计算平均审核时间（小时）
        Double avgAuditHours = allReports.stream()
                .filter(r -> r.getAuditTime() != null && r.getSubmitTime() != null)
                .mapToDouble(r -> java.time.Duration.between(r.getSubmitTime(), r.getAuditTime()).toHours())
                .average()
                .orElse(0.0);

        return ReportAuditDTO.AuditStatistics.builder()
                .totalReports(totalReports)
                .pendingCount(pendingCount)
                .approvedCount(approvedCount)
                .rejectedCount(rejectedCount)
                .publishedCount(publishedCount)
                .approvedUnpublishedCount(approvedUnpublishedCount)
                .avgAuditHours(avgAuditHours)
                .lastAuditTime(lastAuditTime)
                .build();
    }

    @Override
    public ReportAuditDTO.PublishStatistics getPublishStatistics() {
        log.info("获取发布统计信息");

        // 查询所有报表
        List<ReportConfig> allReports = reportConfigMapper.selectList(null);

        int totalReports = allReports.size();
        int publishedCount = 0;
        int unpublishedCount = 0;
        int approvedUnpublishedCount = 0;

        LocalDateTime lastPublishTime = null;

        for (ReportConfig report : allReports) {
            if (report.getIsPublished() != null && report.getIsPublished() == 1) {
                publishedCount++;
                // 更新最后发布时间
                if (report.getPublishTime() != null) {
                    if (lastPublishTime == null || report.getPublishTime().isAfter(lastPublishTime)) {
                        lastPublishTime = report.getPublishTime();
                    }
                }
            } else {
                unpublishedCount++;
                if (report.getApprovalStatus() != null && report.getApprovalStatus() == 3) {
                    approvedUnpublishedCount++;
                }
            }
        }

        return ReportAuditDTO.PublishStatistics.builder()
                .totalReports(totalReports)
                .publishedCount(publishedCount)
                .unpublishedCount(unpublishedCount)
                .approvedUnpublishedCount(approvedUnpublishedCount)
                .lastPublishTime(lastPublishTime)
                .build();
    }

    @Override
    public List<ReportAuditDTO.AuditLog> getAuditHistory(Long reportId, int page, int size) {
        log.info("获取审核历史，reportId: {}, page: {}, size: {}", reportId, page, size);

        // 分页查询审核日志
        int offset = page * size;
        List<ReportAuditLog> auditLogs = auditLogMapper.findByReportIdWithPaging(reportId, offset, size);

        return auditLogs.stream()
                .map(log -> ReportAuditDTO.AuditLog.builder()
                        .id(log.getId())
                        .reportId(log.getReportId())
                        .auditorName(log.getAuditorName())
                        .auditDecision(log.getAuditDecision())
                        .auditComment(log.getAuditComment())
                        .oldStatus(log.getOldStatus())
                        .newStatus(log.getNewStatus())
                        .auditTime(log.getAuditTime())
                        .createdTime(log.getCreatedTime())
                        .build())
                .collect(Collectors.toList());
    }


    private Long getCurrentUserId(Authentication authentication) {
        // 从认证信息中获取用户ID
        // 这里需要根据实际的用户认证实现来获取用户ID
        return 1L; // 临时返回，实际应该从authentication中获取
    }
}
