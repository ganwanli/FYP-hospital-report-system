package com.hospital.report.ai.service;

import com.hospital.report.ai.config.MilvusConfig;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.SearchResults;
import io.milvus.param.*;
import io.milvus.param.collection.*;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.param.index.DropIndexParam;
import io.milvus.grpc.MutationResult;
import io.milvus.grpc.QueryResults;
import io.milvus.response.MutationResultWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Milvus向量存储服务 - 性能优化版
 * 提供高效的向量增删改查功能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorStoreService {
    
    private final MilvusServiceClient milvusClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 集合加载状态缓存
    private final java.util.concurrent.ConcurrentHashMap<String, Boolean> loadedCollections = new java.util.concurrent.ConcurrentHashMap<>();
    
    /**
     * 创建集合
     */
    public boolean createCollection(String collectionName, String description) {
        try {
            // 检查集合是否存在
            R<Boolean> hasCollection = milvusClient.hasCollection(
                HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            if (hasCollection.getData()) {
                log.info("集合 {} 已存在", collectionName);
                return true;
            }
            
            // 定义字段
            List<FieldType> fields = Arrays.asList(
                FieldType.newBuilder()
                    .withName("id")
                    .withDataType(DataType.Int64)
                    .withPrimaryKey(true)
                    .withAutoID(true)
                    .build(),
                FieldType.newBuilder()
                    .withName("source_id")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(255)
                    .build(),
                FieldType.newBuilder()
                    .withName("content")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build(),
                FieldType.newBuilder()
                    .withName("metadata")
                    .withDataType(DataType.VarChar)
                    .withMaxLength(65535)
                    .build(),
                FieldType.newBuilder()
                    .withName("vector")
                    .withDataType(DataType.FloatVector)
                    .withDimension(MilvusConfig.VECTOR_DIMENSION)
                    .build()
            );
            
            // 创建集合Schema
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription(description)
                .withFieldTypes(fields)
                .build();
            
            // 创建集合
            R<RpcStatus> response = milvusClient.createCollection(createCollectionParam);
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("创建集合失败: {}", response.getMessage());
                return false;
            }
            
            // 创建索引
            createIndex(collectionName);
            
            log.info("成功创建集合: {}", collectionName);
            return true;
            
        } catch (Exception e) {
            log.error("创建集合失败: " + collectionName, e);
            return false;
        }
    }
    
    /**
     * 创建向量索引
     */
    private boolean createIndex(String collectionName) {
        try {
            // 创建向量索引
            Map<String, Object> indexParams = new HashMap<>();
            indexParams.put("nlist", MilvusConfig.NLIST);
            
            R<RpcStatus> response = milvusClient.createIndex(
                CreateIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFieldName("vector")
                    .withIndexType(IndexType.valueOf(MilvusConfig.INDEX_TYPE))
                    .withMetricType(MetricType.valueOf(MilvusConfig.METRIC_TYPE))
                    .withExtraParam("{\"nlist\":" + MilvusConfig.NLIST + "}")
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("创建索引失败: {}", response.getMessage());
                return false;
            }
            
            log.info("成功创建索引: {}", collectionName);
            return true;
            
        } catch (Exception e) {
            log.error("创建索引失败: " + collectionName, e);
            return false;
        }
    }
    
    /**
     * 智能加载集合到内存（简化版）
     */
    public boolean loadCollectionOptimized(String collectionName) {
        // 检查缓存，避免重复加载
        if (loadedCollections.getOrDefault(collectionName, false)) {
            log.debug("集合 {} 已在内存中，跳过加载", collectionName);
            return true;
        }
        
        try {
            // 直接尝试加载集合（简化版本，移除状态检查）
            R<RpcStatus> response = milvusClient.loadCollection(
                LoadCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("加载集合失败: {}", response.getMessage());
                return false;
            }
            
            // 更新缓存
            loadedCollections.put(collectionName, true);
            log.info("成功加载集合: {}", collectionName);
            return true;
            
        } catch (Exception e) {
            log.error("加载集合失败: " + collectionName, e);
            return false;
        }
    }
    
    /**
     * 插入向量数据
     */
    public boolean insertVectors(String collectionName, List<VectorData> vectorDataList) {
        try {
            if (vectorDataList.isEmpty()) {
                return true;
            }
            
            // 准备数据
            List<String> sourceIds = new ArrayList<>();
            List<String> contents = new ArrayList<>();
            List<String> metadatas = new ArrayList<>();
            List<List<Float>> vectors = new ArrayList<>();
            
            for (VectorData data : vectorDataList) {
                sourceIds.add(data.getSourceId());
                contents.add(data.getContent());
                metadatas.add(data.getMetadata());
                vectors.add(data.getVector());
            }
            
            // 构建插入数据
            List<InsertParam.Field> fields = Arrays.asList(
                new InsertParam.Field("source_id", sourceIds),
                new InsertParam.Field("content", contents),
                new InsertParam.Field("metadata", metadatas),
                new InsertParam.Field("vector", vectors)
            );
            
            // 执行插入
            R<MutationResult> response = milvusClient.insert(
                InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withFields(fields)
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("插入向量失败: {}", response.getMessage());
                return false;
            }
            
            // 刷新数据到磁盘
            milvusClient.flush(
                FlushParam.newBuilder()
                    .withCollectionNames(Collections.singletonList(collectionName))
                    .build()
            );
            
            log.info("成功插入 {} 个向量到集合: {}", vectorDataList.size(), collectionName);
            return true;
            
        } catch (Exception e) {
            log.error("插入向量失败: " + collectionName, e);
            return false;
        }
    }
    
    /**
     * 高性能向量搜索（优化版）
     */
    public List<SearchResult> searchSimilarVectors(String collectionName, List<Float> queryVector, int topK) {
        try {
            // 使用优化的集合加载
            if (!loadCollectionOptimized(collectionName)) {
                log.error("集合加载失败，无法进行搜索");
                return Collections.emptyList();
            }
            
            // 优化的搜索参数
            long startTime = System.currentTimeMillis();
            
            // 执行搜索
            R<SearchResults> response = milvusClient.search(
                SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withVectorFieldName("vector")
                    .withVectors(Collections.singletonList(queryVector))
                    .withTopK(topK)
                    .withParams("{\"nprobe\":" + MilvusConfig.NPROBE + "}")
                    .withOutFields(Arrays.asList("source_id", "content", "metadata"))
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("搜索向量失败: {}", response.getMessage());
                return Collections.emptyList();
            }
            
            // 高效解析搜索结果
            List<SearchResult> results = parseSearchResultsOptimized(response.getData());
            
            long searchTime = System.currentTimeMillis() - startTime;
            log.info("向量搜索完成，返回 {} 个结果，耗时: {}ms", results.size(), searchTime);
            
            return results;
            
        } catch (Exception e) {
            log.error("搜索向量失败: " + collectionName, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 优化的搜索结果解析方法
     */
    private List<SearchResult> parseSearchResultsOptimized(SearchResults searchResults) {
        ArrayList<SearchResult> results = new ArrayList<>();
        
        try {
            if (searchResults == null || searchResults.getResults() == null) {
                log.debug("搜索结果为空");
                return results;
            }

            io.milvus.grpc.SearchResultData resultData = searchResults.getResults();
            
            // 检查是否有结果
            if (resultData.getIds().getIntId().getDataCount() == 0) {
                log.debug("Milvus搜索结果为空");
                return results;
            }

            // 批量获取数据
            java.util.List<Long> ids = resultData.getIds().getIntId().getDataList();
            java.util.List<Float> scores = resultData.getScoresList();
            
            // 预构建字段数据映射
            java.util.Map<String, java.util.List<String>> fieldsMap = new HashMap<>();
            for (io.milvus.grpc.FieldData fieldData : resultData.getFieldsDataList()) {
                if (fieldData.hasScalars() && fieldData.getScalars().hasStringData()) {
                    fieldsMap.put(fieldData.getFieldName(), 
                        fieldData.getScalars().getStringData().getDataList());
                }
            }
            
            // 批量创建结果对象
            results.ensureCapacity(ids.size());
            for (int i = 0; i < ids.size(); i++) {
                SearchResult result = new SearchResult();
                result.setId(ids.get(i));
                result.setScore(scores.get(i));
                
                // 快速字段赋值
                java.util.List<String> sourceIds = fieldsMap.get("source_id");
                if (sourceIds != null && i < sourceIds.size()) {
                    result.setSourceId(sourceIds.get(i));
                }
                
                java.util.List<String> contents = fieldsMap.get("content");
                if (contents != null && i < contents.size()) {
                    result.setContent(contents.get(i));
                }
                
                java.util.List<String> metadataList = fieldsMap.get("metadata");
                if (metadataList != null && i < metadataList.size()) {
                    result.setMetadata(metadataList.get(i));
                }
                
                results.add(result);
            }
            
            log.debug("高效解析Milvus搜索结果，共{}条", results.size());
            
        } catch (Exception e) {
            log.error("优化解析Milvus搜索结果时出现异常: {}", e.getMessage(), e);
        }
        
        return results;
    }
    /**
     * 优化的数据源查询方法
     */
    public List<SearchResult> searchByDatasourceId(String collectionName, Long datasourceId, int limit) {
        try {
            // 使用优化的集合加载
            if (!loadCollectionOptimized(collectionName)) {
                log.error("集合加载失败，无法进行查询");
                return Collections.emptyList();
            }
            
            long startTime = System.currentTimeMillis();
            
            // 使用查询而不是搜索来获取特定数据源的数据
            R<QueryResults> response = milvusClient.query(
                io.milvus.param.dml.QueryParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr("metadata like \"%datasourceId\":" + datasourceId + "%\"")
                    .withOutFields(Arrays.asList("source_id", "content", "metadata"))
                    .withLimit((long) limit)
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("按数据源查询失败: {}", response.getMessage());
                return Collections.emptyList();
            }
            
            // 高效解析查询结果
            List<SearchResult> results = parseQueryResultsOptimized(response.getData());
            
            long queryTime = System.currentTimeMillis() - startTime;
            log.info("数据源查询完成，返回 {} 个结果，耗时: {}ms", results.size(), queryTime);
            
            return results;
            
        } catch (Exception e) {
            log.error("按数据源查询失败: " + datasourceId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 优化的查询结果解析方法
     */
    private List<SearchResult> parseQueryResultsOptimized(QueryResults queryResults) {
        ArrayList<SearchResult> results = new ArrayList<>();
        
        try {
            if (queryResults == null || queryResults.getFieldsDataCount() == 0) {
                log.debug("查询结果为空");
                return results;
            }

            // 预构建字段数据映射
            java.util.Map<String, java.util.List<String>> fieldsMap = new HashMap<>();
            int resultCount = 0;
            
            for (io.milvus.grpc.FieldData fieldData : queryResults.getFieldsDataList()) {
                if (fieldData.hasScalars() && fieldData.getScalars().hasStringData()) {
                    java.util.List<String> dataList = fieldData.getScalars().getStringData().getDataList();
                    fieldsMap.put(fieldData.getFieldName(), dataList);
                    if (resultCount == 0) {
                        resultCount = dataList.size(); // 使用第一个字段的数量确定结果总数
                    }
                }
            }

            // 批量创建结果对象
            results.ensureCapacity(resultCount);
            for (int i = 0; i < resultCount; i++) {
                SearchResult result = new SearchResult();
                result.setScore(1.0f); // 查询结果没有分数，设为1.0

                // 快速字段赋值
                java.util.List<String> sourceIds = fieldsMap.get("source_id");
                if (sourceIds != null && i < sourceIds.size()) {
                    result.setSourceId(sourceIds.get(i));
                }

                java.util.List<String> contents = fieldsMap.get("content");
                if (contents != null && i < contents.size()) {
                    result.setContent(contents.get(i));
                }

                java.util.List<String> metadataList = fieldsMap.get("metadata");
                if (metadataList != null && i < metadataList.size()) {
                    result.setMetadata(metadataList.get(i));
                }

                results.add(result);
            }

            log.debug("高效解析Milvus查询结果，共{}条", results.size());

        } catch (Exception e) {
            log.error("优化解析Milvus查询结果时出现异常: {}", e.getMessage(), e);
        }

        return results;
    }
    /**
     * 检查集合中是否有指定数据源的数据（优化版）
     */
    public boolean hasDataForDatasource(String collectionName, Long datasourceId) {
        try {
            List<SearchResult> results = searchByDatasourceId(collectionName, datasourceId, 1);
            return !results.isEmpty();
        } catch (Exception e) {
            log.error("检查数据源数据失败: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取数据源的统计信息
     */
    public SchemaStatistics getSchemaStatistics(String collectionName, Long datasourceId) {
        try {
            List<SearchResult> allResults = searchByDatasourceId(collectionName, datasourceId, 10000);
            
            SchemaStatistics stats = new SchemaStatistics();
            stats.setDatasourceId(datasourceId);
            stats.setTotalSchemaRecords((long) allResults.size());
            
            // 统计表和字段数量
            Set<String> tables = new HashSet<>();
            long columnCount = 0;
            
            for (SearchResult result : allResults) {
                try {
                    String metadata = result.getMetadata();
                    if (metadata != null) {
                        // 简单解析metadata JSON
                        if (metadata.contains("\"tableName\"")) {
                            String tableName = extractJsonValue(metadata, "tableName");
                            if (tableName != null && !tableName.isEmpty()) {
                                tables.add(tableName);
                            }
                        }
                        
                        if (metadata.contains("\"columnName\"") && 
                            !metadata.contains("\"columnName\":\"\"")) {
                            columnCount++;
                        }
                    }
                } catch (Exception e) {
                    log.debug("解析metadata失败: {}", e.getMessage());
                }
            }
            
            stats.setTableCount((long) tables.size());
            stats.setColumnCount(columnCount);
            stats.setEmbeddingCount(stats.getTotalSchemaRecords()); // 所有记录都有向量
            stats.setRelationCount(0L); // 暂时不支持关系统计
            
            return stats;
            
        } catch (Exception e) {
            log.error("获取统计信息失败: {}", e.getMessage());
            return createEmptyStatistics(datasourceId);
        }
    }
    
    /**
     * 从JSON字符串中提取值
     */
    private String extractJsonValue(String json, String key) {
        try {
            String pattern = "\"" + key + "\":\"([^\"]*)\";";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(json);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.debug("JSON解析失败: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * 创建空的统计信息
     */
    private SchemaStatistics createEmptyStatistics(Long datasourceId) {
        SchemaStatistics stats = new SchemaStatistics();
        stats.setDatasourceId(datasourceId);
        stats.setTotalSchemaRecords(0L);
        stats.setTableCount(0L);
        stats.setColumnCount(0L);
        stats.setRelationCount(0L);
        stats.setEmbeddingCount(0L);
        return stats;
    }
    
    /**
     * 删除向量数据
     */
    public boolean deleteVectors(String collectionName, String sourceId) {
        try {
            R<MutationResult> response = milvusClient.delete(
                DeleteParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withExpr("source_id == \"" + sourceId + "\"")
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("删除向量失败: {}", response.getMessage());
                return false;
            }
            
            log.info("成功删除向量: {}", sourceId);
            return true;
            
        } catch (Exception e) {
            log.error("删除向量失败: " + sourceId, e);
            return false;
        }
    }
    
    /**
     * 删除集合
     */
    public boolean dropCollection(String collectionName) {
        try {
            // 先删除索引
            milvusClient.dropIndex(
                DropIndexParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            // 删除集合
            R<RpcStatus> response = milvusClient.dropCollection(
                DropCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build()
            );
            
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.error("删除集合失败: {}", response.getMessage());
                return false;
            }
            
            log.info("成功删除集合: {}", collectionName);
            return true;
            
        } catch (Exception e) {
            log.error("删除集合失败: " + collectionName, e);
            return false;
        }
    }
    
    /**
     * 向量数据DTO
     */
    public static class VectorData {
        private String sourceId;
        private String content;
        private String metadata;
        private List<Float> vector;
        
        // 构造器
        public VectorData(String sourceId, String content, String metadata, List<Float> vector) {
            this.sourceId = sourceId;
            this.content = content;
            this.metadata = metadata;
            this.vector = vector;
        }
        
        // Getters and Setters
        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
        
        public List<Float> getVector() { return vector; }
        public void setVector(List<Float> vector) { this.vector = vector; }
    }
    
    /**
     * 搜索结果DTO
     */
    public static class SearchResult {
        private Object id;
        private Float score;
        private String sourceId;
        private String content;
        private String metadata;
        
        // Getters and Setters
        public Object getId() { return id; }
        public void setId(Object id) { this.id = id; }
        
        public Float getScore() { return score; }
        public void setScore(Float score) { this.score = score; }
        
        public String getSourceId() { return sourceId; }
        public void setSourceId(String sourceId) { this.sourceId = sourceId; }
        
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
        
        public String getMetadata() { return metadata; }
        public void setMetadata(String metadata) { this.metadata = metadata; }
    }
    
    /**
     * Schema统计信息类（从DatabaseSchemaService迁移过来）
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