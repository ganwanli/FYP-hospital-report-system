package com.hospital.report.service;

import com.hospital.report.dto.ReportFavoriteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 报表收藏服务接口
 */
public interface ReportFavoriteService {

    /**
     * 添加收藏
     * @param userId 用户ID
     * @param reportId 报表ID
     * @param note 备注
     * @param tags 标签
     * @return 收藏信息
     */
    ReportFavoriteDTO addFavorite(Long userId, Long reportId, String note, String tags);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param reportId 报表ID
     * @return 是否成功
     */
    boolean removeFavorite(Long userId, Long reportId);

    /**
     * 检查是否已收藏
     * @param userId 用户ID
     * @param reportId 报表ID
     * @return 是否已收藏
     */
    boolean isFavorited(Long userId, Long reportId);

    /**
     * 获取用户的所有收藏
     * @param userId 用户ID
     * @return 收藏列表
     */
    List<ReportFavoriteDTO> getUserFavorites(Long userId);

    /**
     * 分页获取用户的收藏
     * @param userId 用户ID
     * @param pageable 分页参数
     * @return 分页收藏列表
     */
    Page<ReportFavoriteDTO> getUserFavorites(Long userId, Pageable pageable);

    /**
     * 根据标签获取用户的收藏
     * @param userId 用户ID
     * @param tag 标签
     * @return 收藏列表
     */
    List<ReportFavoriteDTO> getUserFavoritesByTag(Long userId, String tag);

    /**
     * 更新收藏信息
     * @param favoriteId 收藏ID
     * @param note 备注
     * @param tags 标签
     * @param sortOrder 排序权重
     * @return 更新后的收藏信息
     */
    ReportFavoriteDTO updateFavorite(Long favoriteId, String note, String tags, Integer sortOrder);

    /**
     * 批量更新收藏排序
     * @param userId 用户ID
     * @param favoriteIds 收藏ID列表（按新的排序顺序）
     * @return 是否成功
     */
    boolean updateFavoritesOrder(Long userId, List<Long> favoriteIds);

    /**
     * 获取报表的收藏统计
     * @param reportId 报表ID
     * @return 收藏次数
     */
    Long getReportFavoriteCount(Long reportId);

    /**
     * 获取用户的收藏统计
     * @param userId 用户ID
     * @return 收藏总数
     */
    Long getUserFavoriteCount(Long userId);

    /**
     * 获取用户收藏的报表ID列表
     * @param userId 用户ID
     * @return 报表ID列表
     */
    List<Long> getUserFavoriteReportIds(Long userId);

    /**
     * 清空用户的所有收藏
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean clearUserFavorites(Long userId);

    /**
     * 当报表被删除时，清理相关收藏记录
     * @param reportId 报表ID
     * @return 清理的记录数
     */
    int cleanupFavoritesByReportId(Long reportId);
}
