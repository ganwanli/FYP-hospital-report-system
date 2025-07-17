package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.entity.Dept;
import com.hospital.report.service.DeptService;
import com.hospital.report.vo.DeptTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/system/depts")
@Tag(name = "部门管理", description = "部门相关接口")
@CrossOrigin(origins = "*")
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 获取所有部门
     * @return 部门列表
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有部门", description = "获取所有部门列表")
    public Result<List<Dept>> getAllDepts() {
        try {
            List<Dept> items = deptService.getAllDepts();
            Result<List<Dept>> result = Result.success(items);
            return result;
        } catch (Exception e) {
            log.error("获取部门列表失败", e);
            return Result.error("获取部门列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取部门树
     * @return 部门树
     */
    @GetMapping("/tree")
    @Operation(summary = "获取部门树", description = "获取部门树结构")
    public Result<List<DeptTreeVO>> getDeptTree() {
        try {
            List<DeptTreeVO> deptTree = deptService.getDeptTree();
            return Result.success(deptTree);
        } catch (Exception e) {
            log.error("获取部门树失败", e);
            return Result.error("获取部门树失败: " + e.getMessage());
        }
    }
}