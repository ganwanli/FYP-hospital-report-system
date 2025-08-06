package com.hospital.report.ai.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.HasCollectionParam;

import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus配置类
 * 用于配置Milvus向量数据库连接
 */
@Slf4j
@Configuration
public class MilvusConfig {
    
    @Value("${milvus.host:localhost}")
    private String host;
    
    @Value("${milvus.port:19530}")
    private int port;
    
    @Value("${milvus.database:}")
    private String database;
    
    @Value("${milvus.username:}")
    private String username;
    
    @Value("${milvus.password:}")
    private String password;
    
    @Value("${milvus.connection.timeout:30000}")
    private long connectTimeout;
    
    @Value("${milvus.connection.keepalive:30000}")
    private long keepAliveTimeout;
    
    @Bean
    public MilvusServiceClient milvusClient() {
        try {
            ConnectParam.Builder builder = ConnectParam.newBuilder()
                    .withHost(host)
                    .withPort(port)
                    .withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                    .withKeepAliveTimeout(keepAliveTimeout, TimeUnit.MILLISECONDS);
            
            // 如果配置了数据库名
            if (!database.isEmpty()) {
                builder.withDatabaseName(database);
            }
            
            // 如果配置了用户名和密码
            if (!username.isEmpty() && !password.isEmpty()) {
                builder.withAuthorization(username, password);
            }
            
            ConnectParam connectParam = builder.build();
            MilvusServiceClient client = new MilvusServiceClient(connectParam);
            
            // 测试连接
            HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                    .withCollectionName("test_connection")
                    .build();
            R<Boolean> response = client.hasCollection(hasCollectionParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.warn("Milvus连接测试失败: {}", response.getMessage());
            } else {
                log.info("Milvus连接成功 - 主机: {}:{}", host, port);
            }
            
            return client;
            
        } catch (Exception e) {
            log.error("创建Milvus客户端失败", e);
            throw new RuntimeException("无法连接到Milvus服务器: " + e.getMessage(), e);
        }
    }
    
    /**
     * 向量维度配置
     */
    public static final int VECTOR_DIMENSION = 1536; // OpenAI embedding dimension
    
    /**
     * 集合名称配置
     */
    public static final String SCHEMA_COLLECTION = "schema_vectors";
    public static final String SQL_COLLECTION = "sql_vectors";
    public static final String KNOWLEDGE_COLLECTION = "knowledge_vectors";
    
    /**
     * 索引配置
     */
    public static final String INDEX_TYPE = "IVF_FLAT";
    public static final String METRIC_TYPE = "L2";
    public static final int NLIST = 128;
    public static final int NPROBE = 10;
}