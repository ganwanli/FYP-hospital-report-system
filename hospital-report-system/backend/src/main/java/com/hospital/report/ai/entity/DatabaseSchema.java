package com.hospital.report.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库schema信息
 */
@Data
@TableName("ai_database_schema")
public class DatabaseSchema {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 数据源ID
     */
    private Long datasourceId;
    
    /**
     * 数据库名称
     */
    private String databaseName;
    
    /**
     * 表名
     */
    private String tableName;
    
    /**
     * 表注释/描述
     */
    private String tableComment;
    
    /**
     * 字段名
     */
    private String columnName;
    
    /**
     * 字段类型
     */
    private String columnType;
    
    /**
     * 字段注释
     */
    private String columnComment;
    
    /**
     * 是否主键
     */
    private Boolean isPrimaryKey;
    
    /**
     * 是否允许空值
     */
    private Boolean isNullable;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 完整的描述文本(用于向量化)
     */
    private String fullDescription;
    
    /**
     * 向量嵌入(JSON格式存储)
     */
    private String embedding;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}