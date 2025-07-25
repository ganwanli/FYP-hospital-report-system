package com.hospital.report.controller;

import com.hospital.report.dto.ReportFavoriteDTO;
import com.hospital.report.service.ReportFavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表收藏控制器
 */
@RestController
@RequestMapping("/report-favorites")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportFavoriteController {

    private final ReportFavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addFavorite(@Valid @RequestBody AddFavoriteRequest request) {
        log.info("Adding favorite for user {} and report {}", request.getUserId(), request.getReportId());
        
        Map<String, Object> response = new HashMap<>();
        try {
            ReportFavoriteDTO favorite = favoriteService.addFavorite(
                    request.getUserId(),
                    request.getReportId(),
                    request.getNote(),
                    request.getTags()
            );
            
            response.put("success", true);
            response.put("message", "收藏成功");
            response.put("data", favorite);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to add favorite: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/user/{userId}/report/{reportId}")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long userId,
            @PathVariable Long reportId) {
        log.info("Removing favorite for user {} and report {}", userId, reportId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = favoriteService.removeFavorite(userId, reportId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "取消收藏成功");
            } else {
                response.put("success", false);
                response.put("message", "收藏记录不存在");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to remove favorite: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/user/{userId}/report/{reportId}/check")
    public ResponseEntity<Map<String, Object>> checkFavorite(
            @PathVariable Long userId,
            @PathVariable Long reportId) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isFavorited = favoriteService.isFavorited(userId, reportId);
            
            response.put("success", true);
            response.put("data", Map.of("isFavorited", isFavorited));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to check favorite status: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户的所有收藏
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserFavorites(@PathVariable Long userId) {
        log.info("Getting all favorites for user {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReportFavoriteDTO> favorites = favoriteService.getUserFavorites(userId);
            
            response.put("success", true);
            response.put("data", favorites);
            response.put("total", favorites.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get user favorites: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 分页获取用户的收藏
     */
    @GetMapping("/user/{userId}/page")
    public ResponseEntity<Map<String, Object>> getUserFavorites(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.info("Getting paginated favorites for user {} - page: {}, size: {}", userId, page, size);
        
        Map<String, Object> response = new HashMap<>();
        try {
            Sort sort = Sort.by(sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<ReportFavoriteDTO> favoritePage = favoriteService.getUserFavorites(userId, pageable);
            
            response.put("success", true);
            response.put("data", favoritePage.getContent());
            response.put("page", page);
            response.put("size", size);
            response.put("total", favoritePage.getTotalElements());
            response.put("totalPages", favoritePage.getTotalPages());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get paginated user favorites: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 根据标签获取用户的收藏
     */
    @GetMapping("/user/{userId}/tag/{tag}")
    public ResponseEntity<Map<String, Object>> getUserFavoritesByTag(
            @PathVariable Long userId,
            @PathVariable String tag) {
        
        log.info("Getting favorites by tag '{}' for user {}", tag, userId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            List<ReportFavoriteDTO> favorites = favoriteService.getUserFavoritesByTag(userId, tag);
            
            response.put("success", true);
            response.put("data", favorites);
            response.put("total", favorites.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get user favorites by tag: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新收藏信息
     */
    @PutMapping("/{favoriteId}")
    public ResponseEntity<Map<String, Object>> updateFavorite(
            @PathVariable Long favoriteId,
            @Valid @RequestBody UpdateFavoriteRequest request) {
        
        log.info("Updating favorite with id {}", favoriteId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            ReportFavoriteDTO favorite = favoriteService.updateFavorite(
                    favoriteId,
                    request.getNote(),
                    request.getTags(),
                    request.getSortOrder()
            );
            
            response.put("success", true);
            response.put("message", "更新成功");
            response.put("data", favorite);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update favorite: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量更新收藏排序
     */
    @PutMapping("/user/{userId}/order")
    public ResponseEntity<Map<String, Object>> updateFavoritesOrder(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateOrderRequest request) {
        
        log.info("Updating favorites order for user {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = favoriteService.updateFavoritesOrder(userId, request.getFavoriteIds());
            
            if (success) {
                response.put("success", true);
                response.put("message", "排序更新成功");
            } else {
                response.put("success", false);
                response.put("message", "排序更新失败");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update favorites order: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取报表收藏统计
     */
    @GetMapping("/report/{reportId}/stats")
    public ResponseEntity<Map<String, Object>> getReportFavoriteStats(@PathVariable Long reportId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long favoriteCount = favoriteService.getReportFavoriteCount(reportId);
            
            response.put("success", true);
            response.put("data", Map.of("favoriteCount", favoriteCount));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get report favorite stats: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户收藏统计
     */
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<Map<String, Object>> getUserFavoriteStats(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long favoriteCount = favoriteService.getUserFavoriteCount(userId);
            List<Long> favoriteReportIds = favoriteService.getUserFavoriteReportIds(userId);
            
            response.put("success", true);
            response.put("data", Map.of(
                    "favoriteCount", favoriteCount,
                    "favoriteReportIds", favoriteReportIds
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get user favorite stats: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清空用户的所有收藏
     */
    @DeleteMapping("/user/{userId}/clear")
    public ResponseEntity<Map<String, Object>> clearUserFavorites(@PathVariable Long userId) {
        log.info("Clearing all favorites for user {}", userId);
        
        Map<String, Object> response = new HashMap<>();
        try {
            boolean success = favoriteService.clearUserFavorites(userId);
            
            if (success) {
                response.put("success", true);
                response.put("message", "清空收藏成功");
            } else {
                response.put("success", false);
                response.put("message", "没有收藏记录可清空");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to clear user favorites: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 请求DTO类
    public static class AddFavoriteRequest {
        @NotNull(message = "用户ID不能为空")
        private Long userId;
        
        @NotNull(message = "报表ID不能为空")
        private Long reportId;
        
        private String note;
        private String tags;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getReportId() { return reportId; }
        public void setReportId(Long reportId) { this.reportId = reportId; }
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
    }

    public static class UpdateFavoriteRequest {
        private String note;
        private String tags;
        private Integer sortOrder;

        // Getters and Setters
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    public static class UpdateOrderRequest {
        @NotNull(message = "收藏ID列表不能为空")
        private List<Long> favoriteIds;

        // Getters and Setters
        public List<Long> getFavoriteIds() { return favoriteIds; }
        public void setFavoriteIds(List<Long> favoriteIds) { this.favoriteIds = favoriteIds; }
    }
}
