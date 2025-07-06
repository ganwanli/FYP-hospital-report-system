package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.entity.ReportShare;
import com.hospital.report.service.ReportShareService;
import com.hospital.report.service.ReportGeneratorService;
import com.hospital.report.service.ReportRendererService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/reports/share")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportShareController {
    
    private final ReportShareService reportShareService;
    private final ReportGeneratorService reportGeneratorService;
    private final ReportRendererService reportRendererService;
    
    /**
     * 创建分享
     */
    @PostMapping
    public Result<ReportShare> createShare(@RequestBody ReportShare reportShare) {
        try {
            ReportShare created = reportShareService.createShare(reportShare);
            return Result.success(created);
        } catch (Exception e) {
            return Result.error("创建分享失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新分享
     */
    @PutMapping("/{shareId}")
    public Result<ReportShare> updateShare(@PathVariable Long shareId, @RequestBody ReportShare reportShare) {
        try {
            reportShare.setShareId(shareId);
            ReportShare updated = reportShareService.updateShare(reportShare);
            return Result.success(updated);
        } catch (Exception e) {
            return Result.error("更新分享失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除分享
     */
    @DeleteMapping("/{shareId}")
    public Result<Void> deleteShare(@PathVariable Long shareId) {
        try {
            reportShareService.deleteShare(shareId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除分享失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分享列表
     */
    @GetMapping
    public Result<IPage<ReportShare>> getShareList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) String shareType,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Long createdBy) {
        try {
            Page<ReportShare> pageObj = new Page<>(page, size);
            IPage<ReportShare> result = reportShareService.getShareList(pageObj, reportId, shareType, isActive, createdBy);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取分享列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据分享码获取分享信息
     */
    @GetMapping("/code/{shareCode}")
    public Result<ReportShare> getShareByCode(@PathVariable String shareCode) {
        try {
            ReportShare share = reportShareService.getShareByCode(shareCode);
            if (share == null) {
                return Result.error("分享不存在");
            }
            return Result.success(share);
        } catch (Exception e) {
            return Result.error("获取分享信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 验证分享访问权限
     */
    @PostMapping("/validate/{shareCode}")
    public Result<Map<String, Object>> validateShareAccess(
            @PathVariable String shareCode,
            @RequestBody(required = false) Map<String, Object> request) {
        try {
            String password = null;
            if (request != null) {
                password = (String) request.get("password");
            }
            
            Map<String, Object> result = reportShareService.validateShareAccess(shareCode, password);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("验证分享访问失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取分享统计信息
     */
    @GetMapping("/{shareId}/statistics")
    public Result<Map<String, Object>> getShareStatistics(@PathVariable Long shareId) {
        try {
            Map<String, Object> statistics = reportShareService.getShareStatistics(shareId);
            return Result.success(statistics);
        } catch (Exception e) {
            return Result.error("获取分享统计失败: " + e.getMessage());
        }
    }
    
    /**
     * 刷新分享码
     */
    @PostMapping("/{shareId}/refresh")
    public Result<String> refreshShareCode(@PathVariable Long shareId) {
        try {
            String newShareCode = reportShareService.refreshShareCode(shareId);
            return Result.success(newShareCode);
        } catch (Exception e) {
            return Result.error("刷新分享码失败: " + e.getMessage());
        }
    }
    
    /**
     * 生成分享URL
     */
    @GetMapping("/{shareCode}/url")
    public Result<String> generateShareUrl(@PathVariable String shareCode, HttpServletRequest request) {
        try {
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            baseUrl += request.getContextPath();
            
            String shareUrl = reportShareService.generateShareUrl(shareCode, baseUrl);
            return Result.success(shareUrl);
        } catch (Exception e) {
            return Result.error("生成分享URL失败: " + e.getMessage());
        }
    }
    
    /**
     * 禁用过期分享（管理员接口）
     */
    @PostMapping("/cleanup")
    public Result<Void> disableExpiredShares() {
        try {
            reportShareService.disableExpiredShares();
            return Result.success();
        } catch (Exception e) {
            return Result.error("清理过期分享失败: " + e.getMessage());
        }
    }
}