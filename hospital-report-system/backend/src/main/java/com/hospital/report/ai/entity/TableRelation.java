package com.hospital.report.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 表关系信息
 */
@Data
@TableName("ai_table_relations")
public class TableRelation {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 数据源ID
     */
    private Long datasourceId;
    
    /**
     * 主表名
     */
    private String primaryTable;
    
    /**
     * 外表名
     */
    private String foreignTable;
    
    /**
     * 主表字段
     */
    private String primaryColumn;
    
    /**
     * 外表字段
     */
    private String foreignColumn;
    
    /**
     * 关系类型 (ONE_TO_ONE, ONE_TO_MANY, MANY_TO_MANY)
     */
    private String relationType;
    
    /**
     * 关系描述
     */
    private String relationDescription;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;
}