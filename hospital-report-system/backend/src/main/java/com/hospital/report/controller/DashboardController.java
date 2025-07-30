package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.dto.DashboardCreateDTO;
import com.hospital.report.dto.DashboardElementBatchDTO;
import com.hospital.report.entity.UserDashboard;
import com.hospital.report.entity.UserDashboardElement;
import com.hospital.report.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dashboard管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {
    
    private final DashboardService dashboardService;
    
    /**
     * 获取用户Dashboard列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<UserDashboard>> getUserDashboards(@PathVariable Long userId) {
        try {
            List<UserDashboard> dashboards = dashboardService.getUserDashboards(userId);
            
            // 为每个Dashboard添加元素数量统计
            dashboards.forEach(dashboard -> {
                List<UserDashboardElement> elements = dashboardService.getDashboardElements(dashboard.getId());
                // 可以在这里添加元素数量到响应中，但由于Entity限制，我们暂时在日志中记录
                log.debug("Dashboard {} has {} elements", dashboard.getId(), elements.size());
            });
            
            return Result.success(dashboards);
        } catch (Exception e) {
            log.error("获取用户Dashboard列表失败", e);
            return Result.error("获取Dashboard列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户默认Dashboard
     */
    @GetMapping("/user/{userId}/default")
    public Result<UserDashboard> getDefaultDashboard(@PathVariable Long userId) {
        try {
            UserDashboard dashboard = dashboardService.getDefaultDashboard(userId);
            if (dashboard == null) {
                return Result.error("用户没有默认Dashboard");
            }
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("获取用户默认Dashboard失败", e);
            return Result.error("获取默认Dashboard失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Dashboard
     */
    @PostMapping
    public Result<UserDashboard> createDashboard(@Valid @RequestBody DashboardCreateDTO dto) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            UserDashboard dashboard = dashboardService.createDashboard(userId, dto);
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("创建Dashboard失败", e);
            return Result.error("创建Dashboard失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新Dashboard
     */
    @PutMapping("/{dashboardId}")
    public Result<UserDashboard> updateDashboard(
            @PathVariable Long dashboardId,
            @Valid @RequestBody DashboardCreateDTO dto) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            UserDashboard dashboard = dashboardService.updateDashboard(dashboardId, userId, dto);
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("更新Dashboard失败", e);
            return Result.error("更新Dashboard失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Dashboard
     */
    @DeleteMapping("/{dashboardId}")
    public Result<Boolean> deleteDashboard(@PathVariable Long dashboardId) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            boolean result = dashboardService.deleteDashboard(dashboardId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除Dashboard失败", e);
            return Result.error("删除Dashboard失败: " + e.getMessage());
        }
    }
    
    /**
     * 复制Dashboard
     */
    @PostMapping("/{dashboardId}/copy")
    public Result<UserDashboard> copyDashboard(
            @PathVariable Long dashboardId,
            @RequestBody Map<String, String> requestBody) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            String newName = requestBody.get("name");
            if (newName == null || newName.trim().isEmpty()) {
                return Result.error("新Dashboard名称不能为空");
            }
            
            UserDashboard dashboard = dashboardService.copyDashboard(dashboardId, userId, newName);
            return Result.success(dashboard);
        } catch (Exception e) {
            log.error("复制Dashboard失败", e);
            return Result.error("复制Dashboard失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置默认Dashboard
     */
    @PutMapping("/{dashboardId}/default")
    public Result<Boolean> setDefaultDashboard(@PathVariable Long dashboardId) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            boolean result = dashboardService.setDefaultDashboard(dashboardId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("设置默认Dashboard失败", e);
            return Result.error("设置默认Dashboard失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取Dashboard元素列表
     */
    @GetMapping("/elements/dashboard/{dashboardId}")
    public Result<List<UserDashboardElement>> getDashboardElements(@PathVariable Long dashboardId) {
        try {
            List<UserDashboardElement> elements = dashboardService.getDashboardElements(dashboardId);
            return Result.success(elements);
        } catch (Exception e) {
            log.error("获取Dashboard元素列表失败", e);
            return Result.error("获取元素列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量保存Dashboard元素
     */
    @PostMapping("/elements/dashboard/{dashboardId}/batch")
    public Result<Boolean> saveDashboardElements(
            @PathVariable Long dashboardId,
            @Valid @RequestBody DashboardElementBatchDTO dto) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            boolean result = dashboardService.saveDashboardElements(dashboardId, userId, dto);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量保存Dashboard元素失败", e);
            return Result.error("保存元素失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建Dashboard元素
     */
    @PostMapping("/elements")
    public Result<UserDashboardElement> createElement(@RequestBody Map<String, Object> requestBody) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            Long dashboardId = Long.valueOf(requestBody.get("dashboardId").toString());
            
            // 构建元素数据
            DashboardElementBatchDTO.ElementData elementData = new DashboardElementBatchDTO.ElementData();
            elementData.setElementId(requestBody.get("elementId").toString());
            elementData.setElementType(requestBody.get("elementType").toString());
            elementData.setPositionX(Integer.valueOf(requestBody.get("positionX").toString()));
            elementData.setPositionY(Integer.valueOf(requestBody.get("positionY").toString()));
            elementData.setWidth(Integer.valueOf(requestBody.get("width").toString()));
            elementData.setHeight(Integer.valueOf(requestBody.get("height").toString()));
            
            if (requestBody.containsKey("contentConfig")) {
                elementData.setContentConfig(requestBody.get("contentConfig").toString());
            }
            if (requestBody.containsKey("dataConfig")) {
                elementData.setDataConfig(requestBody.get("dataConfig").toString());
            }
            if (requestBody.containsKey("styleConfig")) {
                elementData.setStyleConfig(requestBody.get("styleConfig").toString());
            }
            if (requestBody.containsKey("sortOrder")) {
                elementData.setSortOrder(Integer.valueOf(requestBody.get("sortOrder").toString()));
            }
            
            UserDashboardElement element = dashboardService.createElement(dashboardId, userId, elementData);
            return Result.success(element);
        } catch (Exception e) {
            log.error("创建Dashboard元素失败", e);
            return Result.error("创建元素失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新Dashboard元素
     */
    @PutMapping("/elements/{elementId}")
    public Result<UserDashboardElement> updateElement(
            @PathVariable Long elementId,
            @RequestBody Map<String, Object> requestBody) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            
            // 构建元素数据
            DashboardElementBatchDTO.ElementData elementData = new DashboardElementBatchDTO.ElementData();
            elementData.setElementId(requestBody.get("elementId").toString());
            elementData.setElementType(requestBody.get("elementType").toString());
            elementData.setPositionX(Integer.valueOf(requestBody.get("positionX").toString()));
            elementData.setPositionY(Integer.valueOf(requestBody.get("positionY").toString()));
            elementData.setWidth(Integer.valueOf(requestBody.get("width").toString()));
            elementData.setHeight(Integer.valueOf(requestBody.get("height").toString()));
            
            if (requestBody.containsKey("contentConfig")) {
                elementData.setContentConfig(requestBody.get("contentConfig").toString());
            }
            if (requestBody.containsKey("dataConfig")) {
                elementData.setDataConfig(requestBody.get("dataConfig").toString());
            }
            if (requestBody.containsKey("styleConfig")) {
                elementData.setStyleConfig(requestBody.get("styleConfig").toString());
            }
            if (requestBody.containsKey("sortOrder")) {
                elementData.setSortOrder(Integer.valueOf(requestBody.get("sortOrder").toString()));
            }
            
            UserDashboardElement element = dashboardService.updateElement(elementId, userId, elementData);
            return Result.success(element);
        } catch (Exception e) {
            log.error("更新Dashboard元素失败", e);
            return Result.error("更新元素失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除Dashboard元素
     */
    @DeleteMapping("/elements/{elementId}")
    public Result<Boolean> deleteElement(@PathVariable Long elementId) {
        try {
            // TODO: 从JWT Token或Session中获取用户ID
            Long userId = 1L; // 临时硬编码，实际应该从认证信息中获取
            boolean result = dashboardService.deleteElement(elementId, userId);
            return Result.success(result);
        } catch (Exception e) {
            log.error("删除Dashboard元素失败", e);
            return Result.error("删除元素失败: " + e.getMessage());
        }
    }
}