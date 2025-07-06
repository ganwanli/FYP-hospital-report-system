package com.hospital.report.controller;

import com.hospital.report.entity.DataSource;
import com.hospital.report.service.DataSourceService;
import com.hospital.report.service.SqlExecutorService;
import com.hospital.report.dto.DataSourceRequest;
import com.hospital.report.dto.SqlExecutionRequest;
import com.hospital.report.dto.SqlExecutionResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataSourceController.class)
@DisplayName("数据源管理控制器测试")
public class DataSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataSourceService dataSourceService;

    @MockBean
    private SqlExecutorService sqlExecutorService;

    @Autowired
    private ObjectMapper objectMapper;

    private DataSource testDataSource;
    private DataSourceRequest dataSourceRequest;
    private SqlExecutionRequest sqlRequest;

    @BeforeEach
    void setUp() {
        testDataSource = new DataSource();
        testDataSource.setId(1L);
        testDataSource.setName("测试数据源");
        testDataSource.setType("MYSQL");
        testDataSource.setHost("localhost");
        testDataSource.setPort(3306);
        testDataSource.setDatabase("hospital_db");
        testDataSource.setUsername("root");
        testDataSource.setPassword("password");
        testDataSource.setStatus("ACTIVE");

        dataSourceRequest = new DataSourceRequest();
        dataSourceRequest.setName("新数据源");
        dataSourceRequest.setType("MYSQL");
        dataSourceRequest.setHost("localhost");
        dataSourceRequest.setPort(3306);
        dataSourceRequest.setDatabase("test_db");
        dataSourceRequest.setUsername("user");
        dataSourceRequest.setPassword("pass");

        sqlRequest = new SqlExecutionRequest();
        sqlRequest.setDataSourceId(1L);
        sqlRequest.setSql("SELECT * FROM users LIMIT 10");
        sqlRequest.setParameters(new HashMap<>());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("获取数据源列表")
    void testGetDataSources() throws Exception {
        List<DataSource> dataSources = Arrays.asList(testDataSource);
        when(dataSourceService.findAll()).thenReturn(dataSources);

        mockMvc.perform(get("/api/datasources"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("测试数据源"))
                .andExpect(jsonPath("$.data[0].type").value("MYSQL"));

        verify(dataSourceService).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("创建数据源成功")
    void testCreateDataSource() throws Exception {
        DataSource newDataSource = new DataSource();
        newDataSource.setId(2L);
        newDataSource.setName("新数据源");
        newDataSource.setType("MYSQL");
        newDataSource.setHost("localhost");
        newDataSource.setPort(3306);
        newDataSource.setDatabase("test_db");
        newDataSource.setUsername("user");
        newDataSource.setStatus("ACTIVE");

        when(dataSourceService.create(any(DataSourceRequest.class))).thenReturn(newDataSource);

        mockMvc.perform(post("/api/datasources")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dataSourceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("新数据源"))
                .andExpect(jsonPath("$.data.type").value("MYSQL"));

        verify(dataSourceService).create(any(DataSourceRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("测试数据源连接")
    void testDataSourceConnection() throws Exception {
        when(dataSourceService.testConnection(1L)).thenReturn(true);

        mockMvc.perform(post("/api/datasources/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(true));

        verify(dataSourceService).testConnection(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("测试数据源连接失败")
    void testDataSourceConnectionFailure() throws Exception {
        when(dataSourceService.testConnection(1L)).thenReturn(false);

        mockMvc.perform(post("/api/datasources/1/test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(false));

        verify(dataSourceService).testConnection(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("执行SQL查询")
    void testExecuteSql() throws Exception {
        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(true);
        result.setColumns(Arrays.asList("id", "name", "email"));
        result.setData(Arrays.asList(
                Arrays.asList(1, "张三", "zhangsan@example.com"),
                Arrays.asList(2, "李四", "lisi@example.com")
        ));
        result.setRowCount(2);
        result.setExecutionTime(150L);

        when(sqlExecutorService.executeSql(any(SqlExecutionRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/datasources/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sqlRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.columns").isArray())
                .andExpect(jsonPath("$.data.columns[0]").value("id"))
                .andExpect(jsonPath("$.data.rowCount").value(2))
                .andExpect(jsonPath("$.data.executionTime").value(150));

        verify(sqlExecutorService).executeSql(any(SqlExecutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("SQL执行失败")
    void testExecuteSqlFailure() throws Exception {
        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(false);
        result.setErrorMessage("SQL语法错误");

        when(sqlExecutorService.executeSql(any(SqlExecutionRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/datasources/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sqlRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.errorMessage").value("SQL语法错误"));

        verify(sqlExecutorService).executeSql(any(SqlExecutionRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("权限不足 - 删除数据源")
    void testDeleteDataSourceInsufficientPermission() throws Exception {
        mockMvc.perform(delete("/api/datasources/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("删除数据源成功")
    void testDeleteDataSource() throws Exception {
        doNothing().when(dataSourceService).delete(1L);

        mockMvc.perform(delete("/api/datasources/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(dataSourceService).delete(1L);
    }

    @Test
    @DisplayName("未认证用户访问")
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/api/datasources"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("SQL注入检测")
    void testSqlInjectionDetection() throws Exception {
        SqlExecutionRequest maliciousRequest = new SqlExecutionRequest();
        maliciousRequest.setDataSourceId(1L);
        maliciousRequest.setSql("SELECT * FROM users; DROP TABLE users;");
        maliciousRequest.setParameters(new HashMap<>());

        SqlExecutionResult result = new SqlExecutionResult();
        result.setSuccess(false);
        result.setErrorMessage("检测到危险SQL语句");

        when(sqlExecutorService.executeSql(any(SqlExecutionRequest.class))).thenReturn(result);

        mockMvc.perform(post("/api/datasources/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maliciousRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.errorMessage").value("检测到危险SQL语句"));
    }
}