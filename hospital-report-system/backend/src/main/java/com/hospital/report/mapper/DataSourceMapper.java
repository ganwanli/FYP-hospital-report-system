package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.DataSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DataSourceMapper extends BaseMapper<DataSource> {

    @Select("SELECT id,datasource_name,datasource_code,datasource_type AS databaseType,driver_class AS driverClassName,connection_url AS jdbcUrl,host,port,database_name,username,password,min_pool_size AS initialSize,max_pool_size AS maxActive,test_query AS validationQuery,connection_timeout,status,description,created_by,created_time,updated_by,updated_time,deleted AS isDeleted FROM sys_datasource WHERE status = 1 AND deleted = 0 ORDER BY created_time DESC")
    List<DataSource> findActiveDataSources();

    @Select("SELECT id,datasource_name,datasource_code,datasource_type AS databaseType,driver_class AS driverClassName,connection_url AS jdbcUrl,host,port,database_name,username,password,min_pool_size AS initialSize,max_pool_size AS maxActive,test_query AS validationQuery,connection_timeout,status,description,created_by,created_time,updated_by,updated_time,deleted AS isDeleted FROM sys_datasource WHERE deleted = 0 LIMIT 1")
    DataSource findDefaultDataSource();

    @Select("SELECT id,datasource_name,datasource_code,datasource_type AS databaseType,driver_class AS driverClassName,connection_url AS jdbcUrl,host,port,database_name,username,password,min_pool_size AS initialSize,max_pool_size AS maxActive,test_query AS validationQuery,connection_timeout,status,description,created_by,created_time,updated_by,updated_time,deleted AS isDeleted FROM sys_datasource WHERE datasource_code = #{code} AND deleted = 0")
    DataSource findByCode(String code);

    @Select("SELECT id,datasource_name,datasource_code,datasource_type AS databaseType,driver_class AS driverClassName,connection_url AS jdbcUrl,host,port,database_name,username,password,min_pool_size AS initialSize,max_pool_size AS maxActive,test_query AS validationQuery,connection_timeout,status,description,created_by,created_time,updated_by,updated_time,deleted AS isDeleted FROM sys_datasource WHERE id = #{d} AND deleted = 0")
    DataSource findById(String id);
}