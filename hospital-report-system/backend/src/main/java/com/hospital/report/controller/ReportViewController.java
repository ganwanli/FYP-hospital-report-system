package com.hospital.report.controller;

import com.hospital.report.common.Result;
import com.hospital.report.service.ReportGeneratorService;
import com.hospital.report.service.ReportRendererService;
import com.hospital.report.service.ExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/reports/view")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportViewController {
//    暂时屏蔽
//
//    private final ReportGeneratorService reportGeneratorService;
//    private final ReportRendererService reportRendererService;
//    private final ExportService exportService;
//
//    /**
//     * 获取报表参数定义
//     */
//    @GetMapping("/{reportId}/parameters")
//    public Result<List<Map<String, Object>>> getParameterDefinitions(@PathVariable Long reportId) {
//        try {
//            List<Map<String, Object>> parameters = reportGeneratorService.getParameterDefinitions(reportId);
//            return Result.success(parameters);
//        } catch (Exception e) {
//            return Result.error("获取参数定义失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 生成报表
//     */
//    @PostMapping("/{reportId}/generate")
//    public Result<Map<String, Object>> generateReport(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> parameters) {
//        try {
//            if (parameters == null) {
//                parameters = new HashMap<>();
//            }
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            return Result.success(reportData);
//        } catch (Exception e) {
//            return Result.error("报表生成失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 渲染HTML报表
//     */
//    @PostMapping("/{reportId}/render/html")
//    public ResponseEntity<String> renderToHtml(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> parameters,
//            @RequestParam(defaultValue = "false") boolean mobile) {
//        try {
//            if (parameters == null) {
//                parameters = new HashMap<>();
//            }
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//
//            Map<String, Object> renderOptions = new HashMap<>();
//            renderOptions.put("mobile", mobile);
//
//            String html;
//            if (mobile) {
//                html = reportRendererService.renderToMobileHtml(reportData, renderOptions);
//            } else {
//                html = reportRendererService.renderToHtml(reportData, renderOptions);
//            }
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_HTML)
//                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
//                .body(html);
//
//        } catch (Exception e) {
//            String errorHtml = "<html><body><h1>报表生成失败</h1><p>" + e.getMessage() + "</p></body></html>";
//            return ResponseEntity.status(500)
//                .contentType(MediaType.TEXT_HTML)
//                .body(errorHtml);
//        }
//    }
//
//    /**
//     * 渲染打印版HTML
//     */
//    @PostMapping("/{reportId}/render/print")
//    public ResponseEntity<String> renderToPrint(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> parameters) {
//        try {
//            if (parameters == null) {
//                parameters = new HashMap<>();
//            }
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            String html = reportRendererService.renderToPrintHtml(reportData, new HashMap<>());
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_HTML)
//                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
//                .body(html);
//
//        } catch (Exception e) {
//            String errorHtml = "<html><body><h1>打印版生成失败</h1><p>" + e.getMessage() + "</p></body></html>";
//            return ResponseEntity.status(500)
//                .contentType(MediaType.TEXT_HTML)
//                .body(errorHtml);
//        }
//    }
//
//    /**
//     * 渲染JSON报表
//     */
//    @PostMapping("/{reportId}/render/json")
//    public Result<String> renderToJson(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> parameters) {
//        try {
//            if (parameters == null) {
//                parameters = new HashMap<>();
//            }
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            String json = reportRendererService.renderToJson(reportData, new HashMap<>());
//
//            return Result.success(json);
//        } catch (Exception e) {
//            return Result.error("JSON渲染失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 导出PDF
//     */
//    @PostMapping("/{reportId}/export/pdf")
//    public ResponseEntity<byte[]> exportToPdf(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> requestBody) {
//        try {
//            Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", new HashMap<>());
//            Map<String, Object> options = (Map<String, Object>) requestBody.getOrDefault("options", new HashMap<>());
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            byte[] pdfBytes = exportService.exportToPdf(reportData, options);
//
//            String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + ".pdf";
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
//                .body(pdfBytes);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                .body(("PDF导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }
//
//    /**
//     * 导出Excel
//     */
//    @PostMapping("/{reportId}/export/excel")
//    public ResponseEntity<byte[]> exportToExcel(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> requestBody) {
//        try {
//            Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", new HashMap<>());
//            Map<String, Object> options = (Map<String, Object>) requestBody.getOrDefault("options", new HashMap<>());
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            byte[] excelBytes = exportService.exportToExcel(reportData, options);
//
//            String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + ".xlsx";
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
//                .body(excelBytes);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                .body(("Excel导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }
//
//    /**
//     * 导出Word
//     */
//    @PostMapping("/{reportId}/export/word")
//    public ResponseEntity<byte[]> exportToWord(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> requestBody) {
//        try {
//            Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", new HashMap<>());
//            Map<String, Object> options = (Map<String, Object>) requestBody.getOrDefault("options", new HashMap<>());
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            byte[] wordBytes = exportService.exportToWord(reportData, options);
//
//            String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + ".docx";
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
//                .body(wordBytes);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                .body(("Word导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }
//
//    /**
//     * 导出CSV
//     */
//    @PostMapping("/{reportId}/export/csv")
//    public ResponseEntity<byte[]> exportToCsv(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> requestBody) {
//        try {
//            Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", new HashMap<>());
//            Map<String, Object> options = (Map<String, Object>) requestBody.getOrDefault("options", new HashMap<>());
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            byte[] csvBytes = exportService.exportToCsv(reportData, options);
//
//            String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + ".csv";
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType("text/csv"))
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
//                .body(csvBytes);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                .body(("CSV导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }
//
//    /**
//     * 导出图片
//     */
//    @PostMapping("/{reportId}/export/image")
//    public ResponseEntity<byte[]> exportToImage(
//            @PathVariable Long reportId,
//            @RequestParam(defaultValue = "PNG") String format,
//            @RequestBody(required = false) Map<String, Object> requestBody) {
//        try {
//            Map<String, Object> parameters = (Map<String, Object>) requestBody.getOrDefault("parameters", new HashMap<>());
//            Map<String, Object> options = (Map<String, Object>) requestBody.getOrDefault("options", new HashMap<>());
//
//            Map<String, Object> reportData = reportGeneratorService.generateReport(reportId, parameters);
//            byte[] imageBytes = exportService.exportToImage(reportData, format, options);
//
//            String fileName = "report_" + reportId + "_" + System.currentTimeMillis() + "." + format.toLowerCase();
//            MediaType mediaType = format.equalsIgnoreCase("PNG") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG;
//
//            return ResponseEntity.ok()
//                .contentType(mediaType)
//                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8) + "\"")
//                .body(imageBytes);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                .body(("图片导出失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }
//
//    /**
//     * 获取支持的导出格式
//     */
//    @GetMapping("/export/formats")
//    public Result<String[]> getSupportedFormats() {
//        try {
//            String[] formats = exportService.getSupportedFormats();
//            return Result.success(formats);
//        } catch (Exception e) {
//            return Result.error("获取导出格式失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 验证报表参数
//     */
//    @PostMapping("/{reportId}/validate")
//    public Result<Boolean> validateParameters(
//            @PathVariable Long reportId,
//            @RequestBody Map<String, Object> parameters) {
//        try {
//            boolean isValid = reportGeneratorService.validateParameters(reportId, parameters);
//            return Result.success(isValid);
//        } catch (Exception e) {
//            return Result.error("参数验证失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 估算报表生成时间
//     */
//    @PostMapping("/{reportId}/estimate")
//    public Result<Long> estimateGenerationTime(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> parameters) {
//        try {
//            if (parameters == null) {
//                parameters = new HashMap<>();
//            }
//
//            long estimatedTime = reportGeneratorService.estimateGenerationTime(reportId, parameters);
//            return Result.success(estimatedTime);
//        } catch (Exception e) {
//            return Result.error("时间估算失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 生成报表缩略图
//     */
//    @PostMapping("/{reportId}/thumbnail")
//    public ResponseEntity<byte[]> generateThumbnail(
//            @PathVariable Long reportId,
//            @RequestBody(required = false) Map<String, Object> parameters) {
//        try {
//            if (parameters == null) {
//                parameters = new HashMap<>();
//            }
//
//            byte[] thumbnailBytes = reportGeneratorService.generateThumbnail(reportId, parameters);
//
//            return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_PNG)
//                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
//                .body(thumbnailBytes);
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500)
//                .body(("缩略图生成失败: " + e.getMessage()).getBytes(StandardCharsets.UTF_8));
//        }
//    }
}