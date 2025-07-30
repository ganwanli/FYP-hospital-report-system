package com.hospital.report.service;

import com.hospital.report.dto.DashboardCreateDTO;
import com.hospital.report.dto.DashboardElementBatchDTO;
import com.hospital.report.entity.UserDashboard;
import com.hospital.report.entity.UserDashboardElement;

import java.util.List;

/**
 * Dashboard服务接口
 */
public interface DashboardService {
    
    /**
     * 获取用户Dashboard列表
     */
    List<UserDashboard> getUserDashboards(Long userId);
    
    /**
     * 获取用户默认Dashboard
     */
    UserDashboard getDefaultDashboard(Long userId);
    
    /**
     * 创建Dashboard
     */
    UserDashboard createDashboard(Long userId, DashboardCreateDTO dto);
    
    /**
     * 更新Dashboard
     */
    UserDashboard updateDashboard(Long dashboardId, Long userId, DashboardCreateDTO dto);
    
    /**
     * 删除Dashboard
     */
    boolean deleteDashboard(Long dashboardId, Long userId);
    
    /**
     * 复制Dashboard
     */
    UserDashboard copyDashboard(Long sourceDashboardId, Long userId, String newName);
    
    /**
     * 设置默认Dashboard
     */
    boolean setDefaultDashboard(Long dashboardId, Long userId);
    
    /**
     * 获取Dashboard元素列表
     */
    List<UserDashboardElement> getDashboardElements(Long dashboardId);
    
    /**
     * 批量保存Dashboard元素
     */
    boolean saveDashboardElements(Long dashboardId, Long userId, DashboardElementBatchDTO dto);
    
    /**
     * 创建Dashboard元素
     */
    UserDashboardElement createElement(Long dashboardId, Long userId, DashboardElementBatchDTO.ElementData elementData);
    
    /**
     * 更新Dashboard元素
     */
    UserDashboardElement updateElement(Long elementId, Long userId, DashboardElementBatchDTO.ElementData elementData);
    
    /**
     * 删除Dashboard元素
     */
    boolean deleteElement(Long elementId, Long userId);
}