package com.hospital.report.service.impl;

import com.hospital.report.dto.DashboardCreateDTO;
import com.hospital.report.dto.DashboardElementBatchDTO;
import com.hospital.report.entity.UserDashboard;
import com.hospital.report.entity.UserDashboardElement;
import com.hospital.report.exception.BusinessException;
import com.hospital.report.repository.UserDashboardElementRepository;
import com.hospital.report.repository.UserDashboardRepository;
import com.hospital.report.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dashboard服务实现类
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {
    
    @Autowired
    private UserDashboardRepository dashboardRepository;
    
    @Autowired
    private UserDashboardElementRepository elementRepository;
    
    @Override
    public List<UserDashboard> getUserDashboards(Long userId) {
        log.info("获取用户Dashboard列表, userId: {}", userId);
        return dashboardRepository.findByUserIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeDesc(userId);
    }
    
    @Override
    public UserDashboard getDefaultDashboard(Long userId) {
        log.info("获取用户默认Dashboard, userId: {}", userId);
        Optional<UserDashboard> defaultDashboard = dashboardRepository.findDefaultDashboardByUserId(userId);
        
        // 如果没有默认Dashboard，获取第一个Dashboard作为默认
        if (defaultDashboard.isEmpty()) {
            List<UserDashboard> dashboards = getUserDashboards(userId);
            if (!dashboards.isEmpty()) {
                return dashboards.get(0);
            }
        }
        
        return defaultDashboard.orElse(null);
    }
    
    @Override
    @Transactional
    public UserDashboard createDashboard(Long userId, DashboardCreateDTO dto) {
        log.info("创建Dashboard, userId: {}, dto: {}", userId, dto);
        
        UserDashboard dashboard = new UserDashboard();
        BeanUtils.copyProperties(dto, dashboard);
        dashboard.setUserId(userId);
        dashboard.setCreatedBy(userId);
        dashboard.setUpdatedBy(userId);
        
        // 如果设置为默认，先清除其他默认状态
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            dashboardRepository.clearOtherDefaultStatus(userId, null);
        } else {
            // 如果是第一个Dashboard，自动设为默认
            long count = dashboardRepository.countByUserIdAndIsDeletedFalse(userId);
            if (count == 0) {
                dashboard.setIsDefault(true);
            }
        }
        
        return dashboardRepository.save(dashboard);
    }
    
    @Override
    @Transactional
    public UserDashboard updateDashboard(Long dashboardId, Long userId, DashboardCreateDTO dto) {
        log.info("更新Dashboard, dashboardId: {}, userId: {}, dto: {}", dashboardId, userId, dto);
        
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("Dashboard不存在或无权限");
        }
        
        UserDashboard dashboard = optionalDashboard.get();
        BeanUtils.copyProperties(dto, dashboard);
        dashboard.setUpdatedBy(userId);
        dashboard.setUpdatedTime(LocalDateTime.now());
        
        // 如果设置为默认，先清除其他默认状态
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            dashboardRepository.clearOtherDefaultStatus(userId, dashboardId);
        }
        
        return dashboardRepository.save(dashboard);
    }
    
    @Override
    @Transactional
    public boolean deleteDashboard(Long dashboardId, Long userId) {
        log.info("删除Dashboard, dashboardId: {}, userId: {}", dashboardId, userId);
        
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("Dashboard不存在或无权限");
        }
        
        UserDashboard dashboard = optionalDashboard.get();
        
        // 检查是否为默认Dashboard
        if (Boolean.TRUE.equals(dashboard.getIsDefault())) {
            // 如果删除的是默认Dashboard，需要设置其他Dashboard为默认
            List<UserDashboard> userDashboards = getUserDashboards(userId);
            if (userDashboards.size() > 1) {
                Optional<UserDashboard> nextDefault = userDashboards.stream()
                    .filter(d -> !d.getId().equals(dashboardId))
                    .findFirst();
                if (nextDefault.isPresent()) {
                    dashboardRepository.setAsDefault(nextDefault.get().getId());
                }
            }
        }
        
        // 软删除Dashboard
        dashboard.setIsDeleted(true);
        dashboard.setUpdatedBy(userId);
        dashboard.setUpdatedTime(LocalDateTime.now());
        dashboardRepository.save(dashboard);
        
        // 删除关联的元素
        elementRepository.deleteByDashboardId(dashboardId);
        
        return true;
    }
    
    @Override
    @Transactional
    public UserDashboard copyDashboard(Long sourceDashboardId, Long userId, String newName) {
        log.info("复制Dashboard, sourceDashboardId: {}, userId: {}, newName: {}", sourceDashboardId, userId, newName);
        
        Optional<UserDashboard> optionalSourceDashboard = dashboardRepository.findByIdAndUserId(sourceDashboardId, userId);
        if (optionalSourceDashboard.isEmpty()) {
            throw new BusinessException("源Dashboard不存在或无权限");
        }
        
        UserDashboard sourceDashboard = optionalSourceDashboard.get();
        
        // 创建新Dashboard
        UserDashboard newDashboard = new UserDashboard();
        BeanUtils.copyProperties(sourceDashboard, newDashboard);
        newDashboard.setId(null);
        newDashboard.setDashboardName(newName);
        newDashboard.setIsDefault(false);
        newDashboard.setCreatedBy(userId);
        newDashboard.setUpdatedBy(userId);
        newDashboard.setVersion(1);
        newDashboard.setCreatedTime(LocalDateTime.now());
        newDashboard.setUpdatedTime(LocalDateTime.now());
        
        UserDashboard savedDashboard = dashboardRepository.save(newDashboard);
        
        // 复制元素
        List<UserDashboardElement> sourceElements = getDashboardElements(sourceDashboardId);
        for (UserDashboardElement sourceElement : sourceElements) {
            UserDashboardElement newElement = new UserDashboardElement();
            BeanUtils.copyProperties(sourceElement, newElement);
            newElement.setId(null);
            newElement.setDashboardId(savedDashboard.getId());
            newElement.setCreatedBy(userId);
            newElement.setUpdatedBy(userId);
            newElement.setVersion(1);
            newElement.setCreatedTime(LocalDateTime.now());
            newElement.setUpdatedTime(LocalDateTime.now());
            
            elementRepository.save(newElement);
        }
        
        return savedDashboard;
    }
    
    @Override
    @Transactional
    public boolean setDefaultDashboard(Long dashboardId, Long userId) {
        log.info("设置默认Dashboard, dashboardId: {}, userId: {}", dashboardId, userId);
        
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("Dashboard不存在或无权限");
        }
        
        // 清除其他默认状态
        dashboardRepository.clearOtherDefaultStatus(userId, dashboardId);
        
        // 设置当前为默认
        dashboardRepository.setAsDefault(dashboardId);
        
        return true;
    }
    
    @Override
    public List<UserDashboardElement> getDashboardElements(Long dashboardId) {
        log.info("获取Dashboard元素列表, dashboardId: {}", dashboardId);
        return elementRepository.findByDashboardIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeAsc(dashboardId);
    }
    
    @Override
    @Transactional
    public boolean saveDashboardElements(Long dashboardId, Long userId, DashboardElementBatchDTO dto) {
        log.info("批量保存Dashboard元素, dashboardId: {}, userId: {}, elementCount: {}", 
                dashboardId, userId, dto.getElements().size());
        
        // 验证Dashboard权限
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("Dashboard不存在或无权限");
        }
        
        // 获取现有元素
        List<UserDashboardElement> existingElements = getDashboardElements(dashboardId);
        List<String> existingElementIds = existingElements.stream()
                .map(UserDashboardElement::getElementId)
                .collect(Collectors.toList());
        
        List<String> newElementIds = dto.getElements().stream()
                .map(DashboardElementBatchDTO.ElementData::getElementId)
                .collect(Collectors.toList());
        
        // 删除不再存在的元素
        List<String> toDeleteIds = existingElementIds.stream()
                .filter(id -> !newElementIds.contains(id))
                .collect(Collectors.toList());
        
        if (!toDeleteIds.isEmpty()) {
            elementRepository.batchDeleteElements(dashboardId, toDeleteIds);
        }
        
        // 更新或创建元素
        for (DashboardElementBatchDTO.ElementData elementData : dto.getElements()) {
            Optional<UserDashboardElement> optionalElement = elementRepository.findByElementIdAndIsDeletedFalse(elementData.getElementId());
            
            if (optionalElement.isPresent()) {
                // 更新现有元素
                UserDashboardElement element = optionalElement.get();
                updateElementFromData(element, elementData, userId);
                elementRepository.save(element);
            } else {
                // 创建新元素
                UserDashboardElement element = createElementFromData(dashboardId, elementData, userId);
                elementRepository.save(element);
            }
        }
        
        return true;
    }
    
    @Override
    public UserDashboardElement createElement(Long dashboardId, Long userId, DashboardElementBatchDTO.ElementData elementData) {
        log.info("创建Dashboard元素, dashboardId: {}, userId: {}, elementId: {}", 
                dashboardId, userId, elementData.getElementId());
        
        // 验证Dashboard权限
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(dashboardId, userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("Dashboard不存在或无权限");
        }
        
        UserDashboardElement element = createElementFromData(dashboardId, elementData, userId);
        return elementRepository.save(element);
    }
    
    @Override
    public UserDashboardElement updateElement(Long elementId, Long userId, DashboardElementBatchDTO.ElementData elementData) {
        log.info("更新Dashboard元素, elementId: {}, userId: {}", elementId, userId);
        
        Optional<UserDashboardElement> optionalElement = elementRepository.findById(elementId);
        if (optionalElement.isEmpty()) {
            throw new BusinessException("元素不存在");
        }
        
        UserDashboardElement element = optionalElement.get();
        
        // 验证Dashboard权限
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(element.getDashboardId(), userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("无权限操作此元素");
        }
        
        updateElementFromData(element, elementData, userId);
        return elementRepository.save(element);
    }
    
    @Override
    public boolean deleteElement(Long elementId, Long userId) {
        log.info("删除Dashboard元素, elementId: {}, userId: {}", elementId, userId);
        
        Optional<UserDashboardElement> optionalElement = elementRepository.findById(elementId);
        if (optionalElement.isEmpty()) {
            throw new BusinessException("元素不存在");
        }
        
        UserDashboardElement element = optionalElement.get();
        
        // 验证Dashboard权限
        Optional<UserDashboard> optionalDashboard = dashboardRepository.findByIdAndUserId(element.getDashboardId(), userId);
        if (optionalDashboard.isEmpty()) {
            throw new BusinessException("无权限删除此元素");
        }
        
        // 软删除
        element.setIsDeleted(true);
        element.setUpdatedBy(userId);
        element.setUpdatedTime(LocalDateTime.now());
        elementRepository.save(element);
        
        return true;
    }
    
    private UserDashboardElement createElementFromData(Long dashboardId, DashboardElementBatchDTO.ElementData elementData, Long userId) {
        UserDashboardElement element = new UserDashboardElement();
        element.setDashboardId(dashboardId);
        element.setElementId(elementData.getElementId());
        element.setElementType(elementData.getElementType());
        element.setPositionX(elementData.getPositionX());
        element.setPositionY(elementData.getPositionY());
        element.setWidth(elementData.getWidth());
        element.setHeight(elementData.getHeight());
        element.setContentConfig(elementData.getContentConfig());
        element.setDataConfig(elementData.getDataConfig());
        element.setStyleConfig(elementData.getStyleConfig());
        element.setSortOrder(elementData.getSortOrder());
        element.setCreatedBy(userId);
        element.setUpdatedBy(userId);
        return element;
    }
    
    private void updateElementFromData(UserDashboardElement element, DashboardElementBatchDTO.ElementData elementData, Long userId) {
        element.setElementType(elementData.getElementType());
        element.setPositionX(elementData.getPositionX());
        element.setPositionY(elementData.getPositionY());
        element.setWidth(elementData.getWidth());
        element.setHeight(elementData.getHeight());
        element.setContentConfig(elementData.getContentConfig());
        element.setDataConfig(elementData.getDataConfig());
        element.setStyleConfig(elementData.getStyleConfig());
        element.setSortOrder(elementData.getSortOrder());
        element.setUpdatedBy(userId);
        element.setUpdatedTime(LocalDateTime.now());
    }
}