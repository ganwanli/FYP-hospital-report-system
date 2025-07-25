package com.hospital.report.service.impl;

import com.hospital.report.dto.ReportFavoriteDTO;
import com.hospital.report.entity.ReportFavorite;
import com.hospital.report.repository.ReportFavoriteRepository;
import com.hospital.report.service.ReportFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 报表收藏服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReportFavoriteServiceImpl implements ReportFavoriteService {

    private final ReportFavoriteRepository favoriteRepository;

    @Override
    public ReportFavoriteDTO addFavorite(Long userId, Long reportId, String note, String tags) {
        log.info("Adding favorite for user {} and report {}", userId, reportId);
        
        // 检查是否已经收藏过
        Optional<ReportFavorite> existingFavorite = favoriteRepository.findByUserIdAndReportId(userId, reportId);
        
        ReportFavorite favorite;
        if (existingFavorite.isPresent()) {
            // 如果已存在但被软删除，则重新激活
            favorite = existingFavorite.get();
            if (!favorite.getIsActive()) {
                favorite.setIsActive(true);
                favorite.setNote(note);
                favorite.setTags(tags);
                log.info("Reactivating existing favorite with id {}", favorite.getId());
            } else {
                throw new RuntimeException("报表已经在收藏列表中");
            }
        } else {
            // 创建新的收藏记录
            favorite = ReportFavorite.builder()
                    .userId(userId)
                    .reportId(reportId)
                    .note(note)
                    .tags(tags)
                    .sortOrder(0)
                    .isActive(true)
                    .build();
            log.info("Creating new favorite for user {} and report {}", userId, reportId);
        }
        
        favorite = favoriteRepository.save(favorite);
        return convertToDTO(favorite);
    }

    @Override
    public boolean removeFavorite(Long userId, Long reportId) {
        log.info("Removing favorite for user {} and report {}", userId, reportId);
        
        int updatedRows = favoriteRepository.softDeleteByUserIdAndReportId(userId, reportId);
        boolean success = updatedRows > 0;
        
        if (success) {
            log.info("Successfully removed favorite for user {} and report {}", userId, reportId);
        } else {
            log.warn("No favorite found to remove for user {} and report {}", userId, reportId);
        }
        
        return success;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFavorited(Long userId, Long reportId) {
        return favoriteRepository.findByUserIdAndReportIdAndIsActiveTrue(userId, reportId).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportFavoriteDTO> getUserFavorites(Long userId) {
        log.info("Getting all favorites for user {}", userId);
        
        List<ReportFavorite> favorites = favoriteRepository.findByUserIdAndIsActiveTrue(userId);
        return favorites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportFavoriteDTO> getUserFavorites(Long userId, Pageable pageable) {
        log.info("Getting paginated favorites for user {} with page {}", userId, pageable.getPageNumber());
        
        Page<ReportFavorite> favoritePage = favoriteRepository.findByUserIdAndIsActiveTrue(userId, pageable);
        return favoritePage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportFavoriteDTO> getUserFavoritesByTag(Long userId, String tag) {
        log.info("Getting favorites by tag '{}' for user {}", tag, userId);
        
        List<ReportFavorite> favorites = favoriteRepository.findByUserIdAndTagsContainingAndIsActiveTrue(userId, tag);
        return favorites.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReportFavoriteDTO updateFavorite(Long favoriteId, String note, String tags, Integer sortOrder) {
        log.info("Updating favorite with id {}", favoriteId);
        
        ReportFavorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new RuntimeException("收藏记录不存在"));
        
        if (!favorite.getIsActive()) {
            throw new RuntimeException("收藏记录已被删除");
        }
        
        if (note != null) {
            favorite.setNote(note);
        }
        if (tags != null) {
            favorite.setTags(tags);
        }
        if (sortOrder != null) {
            favorite.setSortOrder(sortOrder);
        }
        
        favorite = favoriteRepository.save(favorite);
        log.info("Successfully updated favorite with id {}", favoriteId);
        
        return convertToDTO(favorite);
    }

    @Override
    public boolean updateFavoritesOrder(Long userId, List<Long> favoriteIds) {
        log.info("Updating favorites order for user {} with {} items", userId, favoriteIds.size());
        
        try {
            // 为每个收藏设置新的排序权重（权重越大排序越靠前）
            IntStream.range(0, favoriteIds.size())
                    .forEach(index -> {
                        Long favoriteId = favoriteIds.get(index);
                        int sortOrder = favoriteIds.size() - index; // 倒序权重
                        favoriteRepository.updateSortOrder(favoriteId, sortOrder);
                    });
            
            log.info("Successfully updated favorites order for user {}", userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to update favorites order for user {}: {}", userId, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long getReportFavoriteCount(Long reportId) {
        return favoriteRepository.countByReportIdAndIsActiveTrue(reportId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserFavoriteCount(Long userId) {
        return favoriteRepository.countByUserIdAndIsActiveTrue(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getUserFavoriteReportIds(Long userId) {
        return favoriteRepository.findReportIdsByUserId(userId);
    }

    @Override
    public boolean clearUserFavorites(Long userId) {
        log.info("Clearing all favorites for user {}", userId);
        
        int updatedRows = favoriteRepository.softDeleteAllByUserId(userId);
        boolean success = updatedRows > 0;
        
        if (success) {
            log.info("Successfully cleared {} favorites for user {}", updatedRows, userId);
        } else {
            log.info("No favorites found to clear for user {}", userId);
        }
        
        return success;
    }

    @Override
    public int cleanupFavoritesByReportId(Long reportId) {
        log.info("Cleaning up favorites for report {}", reportId);
        
        int updatedRows = favoriteRepository.softDeleteAllByReportId(reportId);
        log.info("Cleaned up {} favorite records for report {}", updatedRows, reportId);
        
        return updatedRows;
    }

    /**
     * 将实体转换为DTO
     */
    private ReportFavoriteDTO convertToDTO(ReportFavorite favorite) {
        return ReportFavoriteDTO.builder()
                .id(favorite.getId())
                .userId(favorite.getUserId())
                .reportId(favorite.getReportId())
                .createdAt(favorite.getCreatedAt())
                .updatedAt(favorite.getUpdatedAt())
                .note(favorite.getNote())
                .tags(favorite.getTags())
                .sortOrder(favorite.getSortOrder())
                .isActive(favorite.getIsActive())
                .build();
    }
}
