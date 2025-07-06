package com.hospital.report.controller;

import com.hospital.report.entity.ReportConfig;
import com.hospital.report.service.ReportConfigService;
import com.hospital.report.service.ReportGeneratorService;
import com.hospital.report.service.ExportService;
import com.hospital.report.dto.ReportConfigRequest;
import com.hospital.report.dto.ReportGenerationResult;
import com.hospital.report.dto.ExportOptions;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@DisplayName("报表管理控制器测试")
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportConfigService reportConfigService;

    @MockBean
    private ReportGeneratorService reportGeneratorService;

    @MockBean
    private ExportService exportService;

    @Autowired
    private ObjectMapper objectMapper;

    private ReportConfig testReport;
    private ReportConfigRequest reportRequest;
    private ReportGenerationResult generationResult;

    @BeforeEach
    void setUp() {
        testReport = new ReportConfig();
        testReport.setId(1L);
        testReport.setName("患者统计报表");
        testReport.setDescription("医院患者统计分析报表");
        testReport.setDataSourceId(1L);
        testReport.setCanvasWidth(800);
        testReport.setCanvasHeight(600);
        testReport.setComponentsJson("[{\"id\":\"1\",\"type\":\"table\"}]");
        testReport.setStatus("ACTIVE");
        testReport.setCreatedAt(LocalDateTime.now());
        testReport.setCreatedBy(1L);

        reportRequest = new ReportConfigRequest();
        reportRequest.setName("新报表");
        reportRequest.setDescription("测试报表");
        reportRequest.setDataSourceId(1L);
        reportRequest.setCanvasWidth(800);
        reportRequest.setCanvasHeight(600);
        reportRequest.setComponentsJson("[{\"id\":\"1\",\"type\":\"chart\"}]");

        generationResult = new ReportGenerationResult();
        generationResult.setReportId(1L);
        generationResult.setReportName("患者统计报表");
        generationResult.setCanvasWidth(800);
        generationResult.setCanvasHeight(600);
        generationResult.setComponents(new ArrayList<>());
        generationResult.setGeneratedAt(LocalDateTime.now().toString());
        generationResult.setGenerationTime(500L);
        generationResult.setFromCache(false);
        generationResult.setComponentCount(1);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("获取报表列表")
    void testGetReports() throws Exception {
        List<ReportConfig> reports = Arrays.asList(testReport);
        when(reportConfigService.findAll()).thenReturn(reports);

        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("患者统计报表"))
                .andExpect(jsonPath("$.data[0].description").value("医院患者统计分析报表"));

        verify(reportConfigService).findAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("根据ID获取报表")
    void testGetReportById() throws Exception {
        when(reportConfigService.findById(1L)).thenReturn(testReport);

        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("患者统计报表"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(reportConfigService).findById(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("报表不存在")
    void testGetReportNotFound() throws Exception {
        when(reportConfigService.findById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/reports/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value("报表不存在"));

        verify(reportConfigService).findById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("创建报表成功")
    void testCreateReport() throws Exception {
        ReportConfig newReport = new ReportConfig();
        newReport.setId(2L);
        newReport.setName("新报表");
        newReport.setDescription("测试报表");
        newReport.setDataSourceId(1L);
        newReport.setStatus("ACTIVE");

        when(reportConfigService.create(any(ReportConfigRequest.class))).thenReturn(newReport);

        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("新报表"))
                .andExpect(jsonPath("$.data.id").value(2));

        verify(reportConfigService).create(any(ReportConfigRequest.class));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("权限不足 - 创建报表")
    void testCreateReportInsufficientPermission() throws Exception {
        mockMvc.perform(post("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("生成报表成功")
    void testGenerateReport() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("startDate", "2023-01-01");
        parameters.put("endDate", "2023-12-31");

        when(reportGeneratorService.generateReport(1L, parameters)).thenReturn(generationResult);

        mockMvc.perform(post("/api/reports/1/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(parameters)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.reportName").value("患者统计报表"))
                .andExpect(jsonPath("$.data.generationTime").value(500))
                .andExpect(jsonPath("$.data.fromCache").value(false));

        verify(reportGeneratorService).generateReport(1L, parameters);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("生成报表失败 - 参数错误")
    void testGenerateReportInvalidParameters() throws Exception {
        Map<String, Object> invalidParameters = new HashMap<>();
        invalidParameters.put("startDate", "invalid-date");

        when(reportGeneratorService.generateReport(1L, invalidParameters))
                .thenThrow(new IllegalArgumentException("日期格式错误"));

        mockMvc.perform(post("/api/reports/1/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidParameters)))
                .andExpected(status().isBadRequest())
                .andExpected(jsonPath("$.success").value(false))
                .andExpected(jsonPath("$.message").value("日期格式错误"));

        verify(reportGeneratorService).generateReport(1L, invalidParameters);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("导出PDF成功")
    void testExportToPdf() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        ExportOptions options = new ExportOptions();
        options.setPageSize("A4");
        options.setOrientation("PORTRAIT");

        byte[] pdfContent = "PDF内容".getBytes();
        when(exportService.exportToPdf(1L, parameters, options)).thenReturn(pdfContent);

        mockMvc.perform(post("/api/reports/1/export/pdf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("parameters", parameters, "options", options))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report_1.pdf\""));

        verify(exportService).exportToPdf(1L, parameters, options);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("导出Excel成功")
    void testExportToExcel() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        ExportOptions options = new ExportOptions();
        options.setSheetName("患者统计");
        options.setIncludeHeader(true);

        byte[] excelContent = "Excel内容".getBytes();
        when(exportService.exportToExcel(1L, parameters, options)).thenReturn(excelContent);

        mockMvc.perform(post("/api/reports/1/export/excel")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("parameters", parameters, "options", options))))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"report_1.xlsx\""));

        verify(exportService).exportToExcel(1L, parameters, options);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("更新报表成功")
    void testUpdateReport() throws Exception {
        ReportConfig updatedReport = new ReportConfig();
        updatedReport.setId(1L);
        updatedReport.setName("更新后的报表");
        updatedReport.setDescription("更新描述");

        when(reportConfigService.update(eq(1L), any(ReportConfigRequest.class))).thenReturn(updatedReport);

        mockMvc.perform(put("/api/reports/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(reportConfigService).update(eq(1L), any(ReportConfigRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("删除报表成功")
    void testDeleteReport() throws Exception {
        doNothing().when(reportConfigService).delete(1L);

        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(reportConfigService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("预览报表")
    void testPreviewReport() throws Exception {
        Map<String, Object> previewData = new HashMap<>();
        previewData.put("title", "预览标题");
        previewData.put("components", Arrays.asList());

        when(reportConfigService.preview(1L)).thenReturn(previewData);

        mockMvc.perform(get("/api/reports/1/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("预览标题"));

        verify(reportConfigService).preview(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("获取报表参数定义")
    void testGetParameterDefinitions() throws Exception {
        List<Map<String, Object>> parameters = Arrays.asList(
                Map.of("name", "startDate", "type", "DATE", "required", true, "label", "开始日期"),
                Map.of("name", "endDate", "type", "DATE", "required", true, "label", "结束日期")
        );

        when(reportGeneratorService.getParameterDefinitions(1L)).thenReturn(parameters);

        mockMvc.perform(get("/api/reports/1/parameters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("startDate"))
                .andExpect(jsonPath("$.data[0].type").value("DATE"));

        verify(reportGeneratorService).getParameterDefinitions(1L);
    }
}