package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {

    @Select("SELECT * FROM sys_datasource WHERE status = 1 AND is_deleted = 0 ORDER BY created_time DESC")
    List<DataSource> findActiveDataSources();

    @Select("SELECT * FROM sys_datasource WHERE is_default = 1 AND is_deleted = 0 LIMIT 1")
    DataSource findDefaultDataSource();

    @Select("SELECT * FROM sys_datasource WHERE datasource_code = #{code} AND is_deleted = 0")
    DataSource findByCode(String code);
}