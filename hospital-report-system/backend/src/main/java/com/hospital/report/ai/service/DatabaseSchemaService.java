package com.hospital.report.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hospital.report.ai.entity.DatabaseSchema;
import com.hospital.report.ai.entity.TableRelation;
import com.hospital.report.ai.mapper.DatabaseSchemaMapper;
import com.hospital.report.ai.mapper.TableRelationMapper;
import com.hospital.report.entity.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库Schema管理服务
 */
@Service
@Slf4j
public class DatabaseSchemaService {

    @Autowired
    private DatabaseSchemaMapper databaseSchemaMapper;
    
    @Autowired
    private TableRelationMapper tableRelationMapper;
    
    @Autowired
    private DatabaseMetadataExtractor metadataExtractor;
    
    @Autowired
    private EmbeddingService embeddingService;

    /**
     * 为数据源创建或更新schema向量数据
     */
    @Transactional
    public void createOrUpdateSchemaVectors(DataSource dataSource) {
        log.info("开始为数据源 {} 创建schema向量数据", dataSource.getDatasourceName());
        
        try {
            // 1. 清除现有数据
            clearExistingData(dataSource.getId());
            
            // 2. 提取数据库元数据
            List<DatabaseSchema> schemas = metadataExtractor.extractDatabaseSchema(dataSource);
            List<TableRelation> relations = metadataExtractor.extractTableRelations(dataSource);
            
            // 3. 生成向量嵌入
            embeddingService.generateEmbeddingsForSchemas(schemas);
            
            // 4. 批量保存schema数据
            for (DatabaseSchema schema : schemas) {
                databaseSchemaMapper.insert(schema);
            }
            
            // 5. 批量保存关系数据
            for (TableRelation relation : relations) {
                tableRelationMapper.insert(relation);
            }
            
            log.info("成功为数据源 {} 创建了 {} 个schema记录和 {} 个关系记录", 
                dataSource.getDatasourceName(), schemas.size(), relations.size());
                
        } catch (Exception e) {
            log.error("为数据源 {} 创建schema向量数据失败: {}", dataSource.getDatasourceName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create schema vectors for datasource: " + dataSource.getDatasourceName(), e);
        }
    }

    /**
     * 清除指定数据源的现有schema数据
     */
    private void clearExistingData(Long datasourceId) {
        log.info("清除数据源 {} 的现有schema数据", datasourceId);
        
        QueryWrapper<DatabaseSchema> schemaQuery = new QueryWrapper<>();
        schemaQuery.eq("datasource_id", datasourceId);
        databaseSchemaMapper.delete(schemaQuery);
        
        QueryWrapper<TableRelation> relationQuery = new QueryWrapper<>();
        relationQuery.eq("datasource_id", datasourceId);
        tableRelationMapper.delete(relationQuery);
    }

    /**
     * 获取指定数据源的所有schema
     */
    public List<DatabaseSchema> getSchemasByDatasourceId(Long datasourceId) {
        QueryWrapper<DatabaseSchema> query = new QueryWrapper<>();
        query.eq("datasource_id", datasourceId);
        return databaseSchemaMapper.selectList(query);
    }

    /**
     * 获取指定数据源的表关系
     */
    public List<TableRelation> getTableRelations(Long datasourceId) {
        QueryWrapper<TableRelation> query = new QueryWrapper<>();
        query.eq("datasource_id", datasourceId);
        return tableRelationMapper.selectList(query);
    }

    /**
     * 获取指定表的所有字段
     */
    public List<DatabaseSchema> getTableColumns(Long datasourceId, String tableName) {
        QueryWrapper<DatabaseSchema> query = new QueryWrapper<>();
        query.eq("datasource_id", datasourceId)
             .eq("table_name", tableName)
             .isNotNull("column_name");
        return databaseSchemaMapper.selectList(query);
    }

    /**
     * 检查数据源是否已有schema数据
     */
    public boolean hasSchemaData(Long datasourceId) {
        QueryWrapper<DatabaseSchema> query = new QueryWrapper<>();
        query.eq("datasource_id", datasourceId);
        return databaseSchemaMapper.selectCount(query) > 0;
    }

    /**
     * 获取数据源的表列表
     */
    public List<String> getTableNames(Long datasourceId) {
        return databaseSchemaMapper.selectTableNames(datasourceId);
    }

    /**
     * 获取schema统计信息
     */
    public SchemaStatistics getSchemaStatistics(Long datasourceId) {
        SchemaStatistics stats = new SchemaStatistics();
        stats.setDatasourceId(datasourceId);
        
        QueryWrapper<DatabaseSchema> schemaQuery = new QueryWrapper<>();
        schemaQuery.eq("datasource_id", datasourceId);
        
        // 总记录数
        stats.setTotalSchemaRecords(databaseSchemaMapper.selectCount(schemaQuery));
        
        // 表数量（columnName为null的记录）
        QueryWrapper<DatabaseSchema> tableQuery = new QueryWrapper<>();
        tableQuery.eq("datasource_id", datasourceId).isNull("column_name");
        stats.setTableCount(databaseSchemaMapper.selectCount(tableQuery));
        
        // 字段数量（columnName不为null的记录）
        QueryWrapper<DatabaseSchema> columnQuery = new QueryWrapper<>();
        columnQuery.eq("datasource_id", datasourceId).isNotNull("column_name");
        stats.setColumnCount(databaseSchemaMapper.selectCount(columnQuery));
        
        // 关系数量
        QueryWrapper<TableRelation> relationQuery = new QueryWrapper<>();
        relationQuery.eq("datasource_id", datasourceId);
        stats.setRelationCount(tableRelationMapper.selectCount(relationQuery));
        
        // 有向量嵌入的记录数
        QueryWrapper<DatabaseSchema> embeddingQuery = new QueryWrapper<>();
        embeddingQuery.eq("datasource_id", datasourceId).isNotNull("embedding");
        stats.setEmbeddingCount(databaseSchemaMapper.selectCount(embeddingQuery));
        
        return stats;
    }

    /**
     * Schema统计信息类
     */
    public static class SchemaStatistics {
        private Long datasourceId;
        private Long totalSchemaRecords;
        private Long tableCount;
        private Long columnCount;
        private Long relationCount;
        private Long embeddingCount;
        
        // Getters and Setters
        public Long getDatasourceId() { return datasourceId; }
        public void setDatasourceId(Long datasourceId) { this.datasourceId = datasourceId; }
        
        public Long getTotalSchemaRecords() { return totalSchemaRecords; }
        public void setTotalSchemaRecords(Long totalSchemaRecords) { this.totalSchemaRecords = totalSchemaRecords; }
        
        public Long getTableCount() { return tableCount; }
        public void setTableCount(Long tableCount) { this.tableCount = tableCount; }
        
        public Long getColumnCount() { return columnCount; }
        public void setColumnCount(Long columnCount) { this.columnCount = columnCount; }
        
        public Long getRelationCount() { return relationCount; }
        public void setRelationCount(Long relationCount) { this.relationCount = relationCount; }
        
        public Long getEmbeddingCount() { return embeddingCount; }
        public void setEmbeddingCount(Long embeddingCount) { this.embeddingCount = embeddingCount; }
    }
}