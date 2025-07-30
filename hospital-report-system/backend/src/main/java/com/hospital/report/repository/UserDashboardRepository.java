package com.hospital.report.repository;

import com.hospital.report.entity.UserDashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDashboardRepository extends JpaRepository<UserDashboard, Long> {
    
    /**
     * 根据用户ID获取Dashboard列表（按排序和创建时间排序）
     */
    @Query(value = "SELECT * FROM user_dashboard WHERE user_id = :userId AND is_deleted = 0 ORDER BY sort_order ASC, created_time DESC", nativeQuery = true)
    List<UserDashboard> findByUserIdAndIsDeletedFalseOrderBySortOrderAscCreatedTimeDesc(@Param("userId") Long userId);
    
    /**
     * 获取用户默认Dashboard
     */
    @Query(value = "SELECT * FROM user_dashboard WHERE user_id = :userId AND is_default = 1 AND is_deleted = 0", nativeQuery = true)
    Optional<UserDashboard> findDefaultDashboardByUserId(@Param("userId") Long userId);
    
    /**
     * 清除用户其他Dashboard的默认状态
     */
    @Modifying
    @Query(value = "UPDATE user_dashboard SET is_default = 0 WHERE user_id = :userId AND id != :dashboardId AND is_deleted = 0", nativeQuery = true)
    int clearOtherDefaultStatus(@Param("userId") Long userId, @Param("dashboardId") Long dashboardId);
    
    /**
     * 设置Dashboard为默认
     */
    @Modifying
    @Query(value = "UPDATE user_dashboard SET is_default = 1 WHERE id = :dashboardId AND is_deleted = 0", nativeQuery = true)
    int setAsDefault(@Param("dashboardId") Long dashboardId);
    
    /**
     * 获取用户Dashboard数量
     */
    @Query(value = "SELECT COUNT(*) FROM user_dashboard WHERE user_id = :userId AND is_deleted = 0", nativeQuery = true)
    long countByUserIdAndIsDeletedFalse(@Param("userId") Long userId);
    
    /**
     * 根据ID和用户ID查找Dashboard（权限验证用）
     */
    @Query(value = "SELECT * FROM user_dashboard WHERE id = :id AND user_id = :userId AND is_deleted = 0", nativeQuery = true)
    Optional<UserDashboard> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}