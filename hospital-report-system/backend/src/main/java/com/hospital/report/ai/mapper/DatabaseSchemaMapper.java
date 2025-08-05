package com.hospital.report.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.ai.entity.DatabaseSchema;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 数据库Schema Mapper
 */
@Mapper
public interface DatabaseSchemaMapper extends BaseMapper<DatabaseSchema> {

    /**
     * 获取指定数据源的所有表名
     */
    @Select("SELECT DISTINCT table_name FROM ai_database_schema WHERE datasource_id = #{datasourceId} AND column_name IS NULL")
    List<String> selectTableNames(Long datasourceId);
}