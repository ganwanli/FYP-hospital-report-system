package com.hospital.report.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.report.annotation.RequiresPermission;
import com.hospital.report.common.Result;
import com.hospital.report.dto.DataSourceCreateRequest;
import com.hospital.report.dto.DataSourceQueryRequest;
import com.hospital.report.dto.DataSourceUpdateRequest;
import com.hospital.report.dto.DataSourceTestRequest;
import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/datasource")
@RequiredArgsConstructor
@Tag(name = "数据源管理", description = "数据源管理相关接口")
public class DataSourceController {

    private final DataSourceService dataSourceService;

    @GetMapping("/page")
    @Operation(summary = "分页查询数据源", description = "分页查询数据源列表")
    @RequiresPermission("DATASOURCE_QUERY")
    public Result<Page<DataSource>> getDataSourcePage(@ModelAttribute DataSourceQueryRequest request) {
        try {
            Page<DataSource> page = new Page<>(request.getCurrent(), request.getSize());
            QueryWrapper<DataSource> queryWrapper = new QueryWrapper<>();
            
            if (request.getDatasourceName() != null && !request.getDatasourceName().isEmpty()) {
                queryWrapper.like("datasource_name", request.getDatasourceName());
            }
            if (request.getDatasourceCode() != null && !request.getDatasourceCode().isEmpty()) {
                queryWrapper.like("datasource_code", request.getDatasourceCode());
            }
            if (request.getDatabaseType() != null && !request.getDatabaseType().isEmpty()) {
                queryWrapper.eq("database_type", request.getDatabaseType());
            }
            if (request.getStatus() != null) {
                queryWrapper.eq("status", request.getStatus());
            }
            
            queryWrapper.orderByDesc("created_time");
            Page<DataSource> result = dataSourceService.page(page, queryWrapper);
            
            // 隐藏密码信息
            result.getRecords().forEach(ds -> ds.setPassword("******"));
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询数据源失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    @Operation(summary = "查询数据源列表", description = "查询所有活跃数据源列表")
    @RequiresPermission("DATASOURCE_QUERY")
    public Result<List<DataSource>> getDataSourceList() {
        try {
            List<DataSource> dataSources = dataSourceService.findActiveDataSources();
            // 隐藏密码信息
            dataSources.forEach(ds -> ds.setPassword("******"));
            return Result.success(dataSources);
        } catch (Exception e) {
            log.error("查询数据源列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @GetMapping("/active")
    @Operation(summary = "获取活跃数据源", description = "获取所有活跃数据源列表，无需认证")
    // 移除 @RequiresPermission 注解，允许未认证用户访问
    public Result<List<DataSource>> getActiveDataSources() {
        try {
            List<DataSource> dataSources = dataSourceService.findActiveDataSources();
            // 隐藏敏感信息
            dataSources.forEach(ds -> {
                ds.setPassword("******");
                ds.setUsername("******");
            });
            return Result.success(dataSources);
        } catch (Exception e) {
            log.error("获取活跃数据源失败", e);
            return Result.error("获取活跃数据源失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询数据源", description = "根据数据源ID查询数据源详情")
    @RequiresPermission("DATASOURCE_QUERY")
    public Result<DataSource> getDataSourceById(@PathVariable Long id) {
        try {
            DataSource dataSource = dataSourceService.getById(id);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            // 隐藏密码信息
            dataSource.setPassword("******");
            return Result.success(dataSource);
        } catch (Exception e) {
            log.error("根据ID查询数据源失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    @PostMapping
    @Operation(summary = "新增数据源", description = "新增数据源")
    @RequiresPermission("DATASOURCE_CREATE")
    public Result<Void> createDataSource(@Valid @RequestBody DataSourceCreateRequest request) {
        try {
            DataSource dataSource = new DataSource();
            dataSource.setDatasourceName(request.getDatasourceName());
            dataSource.setDatasourceCode(request.getDatasourceCode());
            dataSource.setDatabaseType(request.getDatabaseType());
            dataSource.setDriverClassName(request.getDriverClassName());
            dataSource.setJdbcUrl(request.getJdbcUrl());
            dataSource.setUsername(request.getUsername());
            dataSource.setPassword(request.getPassword());
            dataSource.setInitialSize(request.getInitialSize());
            dataSource.setMinIdle(request.getMinIdle());
            dataSource.setMaxActive(request.getMaxActive());
            dataSource.setMaxWait(request.getMaxWait());
            dataSource.setConnectionTimeout(request.getConnectionTimeout());
            dataSource.setIdleTimeout(request.getIdleTimeout());
            dataSource.setMaxLifetime(request.getMaxLifetime());
            dataSource.setLeakDetectionThreshold(request.getLeakDetectionThreshold());
            dataSource.setValidationQuery(request.getValidationQuery());
            dataSource.setTestWhileIdle(request.getTestWhileIdle());
            dataSource.setTestOnBorrow(request.getTestOnBorrow());
            dataSource.setTestOnReturn(request.getTestOnReturn());
            dataSource.setIsDefault(request.getIsDefault());
            dataSource.setDescription(request.getDescription());
            
            boolean success = dataSourceService.createDataSource(dataSource);
            if (success) {
                return Result.success();
            } else {
                return Result.error("新增数据源失败");
            }
        } catch (Exception e) {
            log.error("新增数据源失败", e);
            return Result.error("新增失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新数据源", description = "更新数据源信息")
    @RequiresPermission("DATASOURCE_UPDATE")
    public Result<Void> updateDataSource(@PathVariable Long id, @Valid @RequestBody DataSourceUpdateRequest request) {
        try {
            DataSource dataSource = dataSourceService.getById(id);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            
            dataSource.setId(id);
            dataSource.setDatasourceName(request.getDatasourceName());
            dataSource.setDatabaseType(request.getDatabaseType());
            dataSource.setDriverClassName(request.getDriverClassName());
            dataSource.setJdbcUrl(request.getJdbcUrl());
            dataSource.setUsername(request.getUsername());
            if (request.getPassword() != null && !request.getPassword().isEmpty() && !"******".equals(request.getPassword())) {
                dataSource.setPassword(request.getPassword());
            }
            dataSource.setInitialSize(request.getInitialSize());
            dataSource.setMinIdle(request.getMinIdle());
            dataSource.setMaxActive(request.getMaxActive());
            dataSource.setMaxWait(request.getMaxWait());
            dataSource.setConnectionTimeout(request.getConnectionTimeout());
            dataSource.setIdleTimeout(request.getIdleTimeout());
            dataSource.setMaxLifetime(request.getMaxLifetime());
            dataSource.setLeakDetectionThreshold(request.getLeakDetectionThreshold());
            dataSource.setValidationQuery(request.getValidationQuery());
            dataSource.setTestWhileIdle(request.getTestWhileIdle());
            dataSource.setTestOnBorrow(request.getTestOnBorrow());
            dataSource.setTestOnReturn(request.getTestOnReturn());
            dataSource.setIsDefault(request.getIsDefault());
            dataSource.setDescription(request.getDescription());
            
            boolean success = dataSourceService.updateDataSource(dataSource);
            if (success) {
                return Result.success();
            } else {
                return Result.error("更新数据源失败");
            }
        } catch (Exception e) {
            log.error("更新数据源失败", e);
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除数据源", description = "删除数据源")
    @RequiresPermission("DATASOURCE_DELETE")
    public Result<Void> deleteDataSource(@PathVariable Long id) {
        try {
            DataSource dataSource = dataSourceService.getById(id);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            
            boolean success = dataSourceService.deleteDataSource(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除数据源失败");
            }
        } catch (Exception e) {
            log.error("删除数据源失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "测试数据源连接", description = "测试数据源连接")
    @RequiresPermission("DATASOURCE_TEST")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        try {
            boolean success = dataSourceService.testConnection(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("测试数据源连接失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/test")
    @Operation(summary = "测试数据源连接", description = "测试数据源连接配置")
    @RequiresPermission("DATASOURCE_TEST")
    public Result<Boolean> testConnectionConfig(@Valid @RequestBody DataSourceCreateRequest request) {
        try {
            DataSource dataSource = new DataSource();
            dataSource.setDatabaseType(request.getDatabaseType());
            dataSource.setDriverClassName(request.getDriverClassName());
            dataSource.setJdbcUrl(request.getJdbcUrl());
            dataSource.setUsername(request.getUsername());
            dataSource.setPassword(request.getPassword());
            dataSource.setValidationQuery(request.getValidationQuery());

            boolean success = dataSourceService.testConnection(dataSource);
            return Result.success(success);
        } catch (Exception e) {
            log.error("测试数据源连接失败", e);
            return Result.error("测试失败: " + e.getMessage());
        }
    }

    @PostMapping("/test-simple")
    @Operation(summary = "简单测试数据源连接", description = "使用简化参数测试数据源连接")
    public Result<Map<String, Object>> testConnectionSimple(@Valid @RequestBody DataSourceTestRequest request) {
        try {
            log.info("开始测试数据源连接: {} - {}:{}/{}",
                request.getDatabaseType(), request.getHost(), request.getPort(), request.getDatabase());

            DataSource dataSource = new DataSource();
            dataSource.setDatabaseType(request.getDatabaseType());
            dataSource.setDriverClassName(request.getDriverClassName());
            dataSource.setJdbcUrl(request.generateJdbcUrl());
            dataSource.setUsername(request.getUsername());
            dataSource.setPassword(request.getPassword());
            dataSource.setValidationQuery(request.getValidationQuery());
            dataSource.setConnectionTimeout(request.getTimeout() * 1000L); // 转换为毫秒

            long startTime = System.currentTimeMillis();
            boolean success = dataSourceService.testConnection(dataSource);
            long endTime = System.currentTimeMillis();

            Map<String, Object> result = new HashMap<>();
            result.put("success", success);
            result.put("responseTime", endTime - startTime);
            result.put("jdbcUrl", request.generateJdbcUrl());
            result.put("driverClass", request.getDriverClassName());

            if (success) {
                log.info("数据源连接测试成功: {} - {}ms", request.getDatabaseType(), endTime - startTime);
                result.put("message", "Connection test successful");
                result.put("messageZh", "连接测试成功");
                return Result.success(result);
            } else {
                log.warn("数据源连接测试失败: {}", request.getDatabaseType());
                result.put("message", "Connection test failed: 操作失败");
                result.put("messageZh", "连接测试失败: 操作失败");
                return Result.error("Connection test failed: 操作失败", result);
            }

        } catch (Exception e) {
            log.error("测试数据源连接异常: {} - {}", request.getDatabaseType(), e.getMessage(), e);

            String errorMessage = "Connection test failed: ";
            String errorMessageZh = "连接测试失败: ";

            // 根据异常类型提供具体的错误信息
            if (e.getMessage().contains("Communications link failure")) {
                errorMessage += "无法连接到数据库服务器，请检查主机地址和端口";
                errorMessageZh += "无法连接到数据库服务器，请检查主机地址和端口";
            } else if (e.getMessage().contains("Access denied")) {
                errorMessage += "用户名或密码错误";
                errorMessageZh += "用户名或密码错误";
            } else if (e.getMessage().contains("Unknown database")) {
                errorMessage += "数据库不存在";
                errorMessageZh += "数据库不存在";
            } else if (e.getMessage().contains("timeout")) {
                errorMessage += "连接超时";
                errorMessageZh += "连接超时";
            } else {
                errorMessage += e.getMessage();
                errorMessageZh += e.getMessage();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", errorMessage);
            result.put("messageZh", errorMessageZh);
            result.put("error", e.getClass().getSimpleName());
            result.put("jdbcUrl", request.generateJdbcUrl());

            return Result.error(errorMessage, result);
        }
    }

    @PostMapping("/{id}/refresh")
    @Operation(summary = "刷新数据源", description = "刷新数据源连接池")
    @RequiresPermission("DATASOURCE_MANAGE")
    public Result<Void> refreshDataSource(@PathVariable Long id) {
        try {
            DataSource dataSource = dataSourceService.getById(id);
            if (dataSource == null) {
                return Result.error("数据源不存在");
            }
            
            dataSourceService.refreshDataSource(dataSource.getDatasourceCode());
            return Result.success();
        } catch (Exception e) {
            log.error("刷新数据源失败", e);
            return Result.error("刷新失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "获取数据源统计", description = "获取所有数据源连接池统计信息")
    @RequiresPermission("DATASOURCE_MONITOR")
    public Result<List<Map<String, Object>>> getDataSourceStats() {
        try {
            List<Map<String, Object>> stats = dataSourceService.getAllDataSourceStats();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取数据源统计失败", e);
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats/{code}")
    @Operation(summary = "获取数据源统计", description = "获取指定数据源连接池统计信息")
    @RequiresPermission("DATASOURCE_MONITOR")
    public Result<Map<String, Object>> getDataSourceStats(@PathVariable String code) {
        try {
            Map<String, Object> stats = dataSourceService.getDataSourceStats(code);
            if (stats == null) {
                return Result.error("数据源不存在或未激活");
            }
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取数据源统计失败", e);
            return Result.error("获取统计失败: " + e.getMessage());
        }
    }

    @GetMapping("/types")
    @Operation(summary = "获取支持的数据库类型", description = "获取系统支持的数据库类型列表")
    public Result<List<String>> getSupportedDatabaseTypes() {
        try {
            List<String> types = dataSourceService.getSupportedDatabaseTypes();
            return Result.success(types);
        } catch (Exception e) {
            log.error("获取数据库类型失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }

    @GetMapping("/driver/{type}")
    @Operation(summary = "获取数据库驱动", description = "根据数据库类型获取驱动类名")
    public Result<String> getDriverClassName(@PathVariable String type) {
        try {
            String driverClassName = dataSourceService.getDriverClassName(type);
            return Result.success(driverClassName);
        } catch (Exception e) {
            log.error("获取数据库驱动失败", e);
            return Result.error("获取失败: " + e.getMessage());
        }
    }
}