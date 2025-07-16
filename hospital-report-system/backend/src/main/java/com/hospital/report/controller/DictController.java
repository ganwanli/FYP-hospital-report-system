package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.entity.DictItem;
import com.hospital.report.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "字典管理", description = "字典数据管理接口")
public class DictController {

    private final DictService dictService;

    /**
     * 获取字典项列表
     * @param dictType 字典类型
     * @return 字典项列表
     */
    @GetMapping("/system/dict/items/{dictType}")
    @Operation(summary = "获取字典项", description = "根据字典类型获取字典项列表")
    // 移除 @RequiresPermission("DICT_QUERY") 注解，允许未认证用户访问
    public Result<List<DictItem>> getDictItems(@PathVariable String dictType) {
        log.info("开始处理字典项请求: {}", dictType);
        try {
            List<DictItem> items = dictService.getDictItemsByType(dictType);
            log.info("获取字典项成功: {}, 数量: {}", dictType, items != null ? items.size() : 0);
            Result<List<DictItem>> result = Result.success(items);
            log.info("构建响应成功: {}", dictType);
            return result;
        } catch (Exception e) {
            log.error("获取字典项失败: {}", dictType, e);
            return Result.error("获取字典项失败: " + e.getMessage());
        }
    }
}
