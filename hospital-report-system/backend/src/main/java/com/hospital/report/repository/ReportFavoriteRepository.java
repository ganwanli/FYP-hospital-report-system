package com.hospital.report.repository;

import com.hospital.report.entity.ReportFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 报表收藏数据访问层
 */
@Repository
public interface ReportFavoriteRepository extends JpaRepository<ReportFavorite, Long> {

    /**
     * 根据用户ID查找所有收藏的报表（按创建时间倒序）
     */
    @Query("SELECT rf FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.isActive = true ORDER BY rf.sortOrder DESC, rf.createdAt DESC")
    List<ReportFavorite> findByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    /**
     * 根据用户ID分页查找收藏的报表
     */
    @Query("SELECT rf FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.isActive = true")
    Page<ReportFavorite> findByUserIdAndIsActiveTrue(@Param("userId") Long userId, Pageable pageable);

    /**
     * 检查用户是否已收藏某个报表
     */
    @Query("SELECT rf FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.reportId = :reportId AND rf.isActive = true")
    Optional<ReportFavorite> findByUserIdAndReportIdAndIsActiveTrue(@Param("userId") Long userId, @Param("reportId") Long reportId);

    /**
     * 根据报表ID查找所有收藏该报表的用户
     */
    @Query("SELECT rf FROM ReportFavorite rf WHERE rf.reportId = :reportId AND rf.isActive = true")
    List<ReportFavorite> findByReportIdAndIsActiveTrue(@Param("reportId") Long reportId);

    /**
     * 统计用户收藏的报表数量
     */
    @Query("SELECT COUNT(rf) FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.isActive = true")
    Long countByUserIdAndIsActiveTrue(@Param("userId") Long userId);

    /**
     * 统计报表被收藏的次数
     */
    @Query("SELECT COUNT(rf) FROM ReportFavorite rf WHERE rf.reportId = :reportId AND rf.isActive = true")
    Long countByReportIdAndIsActiveTrue(@Param("reportId") Long reportId);

    /**
     * 根据标签查找用户的收藏
     */
    @Query("SELECT rf FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.tags LIKE %:tag% AND rf.isActive = true ORDER BY rf.sortOrder DESC, rf.createdAt DESC")
    List<ReportFavorite> findByUserIdAndTagsContainingAndIsActiveTrue(@Param("userId") Long userId, @Param("tag") String tag);

    /**
     * 软删除收藏记录
     */
    @Modifying
    @Query("UPDATE ReportFavorite rf SET rf.isActive = false WHERE rf.userId = :userId AND rf.reportId = :reportId")
    int softDeleteByUserIdAndReportId(@Param("userId") Long userId, @Param("reportId") Long reportId);

    /**
     * 批量软删除用户的所有收藏
     */
    @Modifying
    @Query("UPDATE ReportFavorite rf SET rf.isActive = false WHERE rf.userId = :userId")
    int softDeleteAllByUserId(@Param("userId") Long userId);

    /**
     * 批量软删除某个报表的所有收藏记录
     */
    @Modifying
    @Query("UPDATE ReportFavorite rf SET rf.isActive = false WHERE rf.reportId = :reportId")
    int softDeleteAllByReportId(@Param("reportId") Long reportId);

    /**
     * 更新收藏的排序权重
     */
    @Modifying
    @Query("UPDATE ReportFavorite rf SET rf.sortOrder = :sortOrder WHERE rf.id = :id")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取用户收藏的报表ID列表
     */
    @Query("SELECT rf.reportId FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.isActive = true ORDER BY rf.sortOrder DESC, rf.createdAt DESC")
    List<Long> findReportIdsByUserId(@Param("userId") Long userId);

    /**
     * 检查收藏记录是否存在（包括已删除的）
     */
    @Query("SELECT rf FROM ReportFavorite rf WHERE rf.userId = :userId AND rf.reportId = :reportId")
    Optional<ReportFavorite> findByUserIdAndReportId(@Param("userId") Long userId, @Param("reportId") Long reportId);
}
