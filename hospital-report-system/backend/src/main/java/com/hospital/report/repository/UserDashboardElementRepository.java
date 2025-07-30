package com.hospital.report.repository;

import com.hospital.report.entity.UserDashboardElement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDashboardElementRepository extends JpaRepository<UserDashboardElement, Long> {
    
    /**
     * 根据Dashboard ID获取元素列表（按排序和创建时间排序）
     */
    @Query(value = "SELECT * FROM user_dashboard_elements WHERE dashboard_id = :dashboardId AND is_deleted = 0 ORDER BY sort_order ASC, created_time ASC", nativeQuery = true)
    List<UserDashboardElement> findByDashboardIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeAsc(@Param("dashboardId") Long dashboardId);
    
    /**
     * 根据Dashboard ID软删除所有元素
     */
    @Modifying
    @Query(value = "UPDATE user_dashboard_elements SET is_deleted = 1 WHERE dashboard_id = :dashboardId", nativeQuery = true)
    int deleteByDashboardId(@Param("dashboardId") Long dashboardId);
    
    /**
     * 根据元素ID获取元素
     */
    @Query(value = "SELECT * FROM user_dashboard_elements WHERE element_id = :elementId AND is_deleted = 0", nativeQuery = true)
    Optional<UserDashboardElement> findByElementIdAndIsDeletedFalse(@Param("elementId") String elementId);
    
    /**
     * 批量软删除Dashboard元素
     */
    @Modifying
    @Query(value = "UPDATE user_dashboard_elements SET is_deleted = 1 WHERE dashboard_id = :dashboardId AND element_id IN (:elementIds)", nativeQuery = true)
    int batchDeleteElements(@Param("dashboardId") Long dashboardId, @Param("elementIds") List<String> elementIds);
    
    /**
     * 获取Dashboard元素数量
     */
    @Query(value = "SELECT COUNT(*) FROM user_dashboard_elements WHERE dashboard_id = :dashboardId AND is_deleted = 0", nativeQuery = true)
    long countByDashboardIdAndIsDeletedFalse(@Param("dashboardId") Long dashboardId);
    
    /**
     * 根据Dashboard ID和元素ID查找元素（用于验证权限）
     */
    @Query(value = "SELECT * FROM user_dashboard_elements WHERE dashboard_id = :dashboardId AND element_id = :elementId AND is_deleted = 0", nativeQuery = true)
    Optional<UserDashboardElement> findByDashboardIdAndElementId(@Param("dashboardId") Long dashboardId, @Param("elementId") String elementId);
}