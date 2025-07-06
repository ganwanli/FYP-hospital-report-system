package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.ReportDataSource;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReportDataSourceMapper extends BaseMapper<ReportDataSource> {

    @Select("SELECT * FROM report_data_source WHERE report_id = #{reportId} ORDER BY created_time ASC")
    List<ReportDataSource> selectByReportId(@Param("reportId") Long reportId);

    @Delete("DELETE FROM report_data_source WHERE report_id = #{reportId}")
    int deleteByReportId(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_data_source WHERE data_source_id = #{dataSourceId}")
    ReportDataSource selectByDataSourceId(@Param("dataSourceId") Long dataSourceId);

    @Update("UPDATE report_data_source SET " +
            "last_refresh_time = NOW(), " +
            "error_message = #{errorMessage}, " +
            "error_count = #{errorCount}, " +
            "updated_time = NOW() " +
            "WHERE data_source_id = #{dataSourceId}")
    int updateRefreshStatus(@Param("dataSourceId") Long dataSourceId,
                           @Param("errorMessage") String errorMessage,
                           @Param("errorCount") Integer errorCount);

    @Update("UPDATE report_data_source SET is_active = #{isActive}, updated_time = NOW() WHERE data_source_id = #{dataSourceId}")
    int updateDataSourceStatus(@Param("dataSourceId") Long dataSourceId, @Param("isActive") Boolean isActive);

    @Select("SELECT * FROM report_data_source WHERE source_name = #{sourceName} AND report_id = #{reportId} AND data_source_id != #{dataSourceId}")
    List<ReportDataSource> selectBySourceNameExcludeId(@Param("sourceName") String sourceName,
                                                       @Param("reportId") Long reportId,
                                                       @Param("dataSourceId") Long dataSourceId);

    @Select("SELECT COUNT(*) FROM report_data_source WHERE report_id = #{reportId}")
    int countByReportId(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_data_source WHERE is_active = true AND cache_enabled = true AND last_refresh_time < DATE_SUB(NOW(), INTERVAL cache_duration MINUTE)")
    List<ReportDataSource> selectExpiredCacheDataSources();

    @Select("SELECT source_type, COUNT(*) as count FROM report_data_source WHERE report_id = #{reportId} GROUP BY source_type")
    List<java.util.Map<String, Object>> selectDataSourceTypeStatistics(@Param("reportId") Long reportId);

    @Insert("<script>" +
            "INSERT INTO report_data_source " +
            "(report_id, source_name, source_type, connection_config, query_config, sql_template_id, " +
            "api_config, static_data, refresh_interval, cache_enabled, cache_duration, parameters_config, " +
            "transform_config, is_active, created_time, updated_time) " +
            "VALUES " +
            "<foreach collection='dataSources' item='ds' separator=','>" +
            "(#{ds.reportId}, #{ds.sourceName}, #{ds.sourceType}, #{ds.connectionConfig}, #{ds.queryConfig}, " +
            "#{ds.sqlTemplateId}, #{ds.apiConfig}, #{ds.staticData}, #{ds.refreshInterval}, #{ds.cacheEnabled}, " +
            "#{ds.cacheDuration}, #{ds.parametersConfig}, #{ds.transformConfig}, #{ds.isActive}, " +
            "#{ds.createdTime}, #{ds.updatedTime})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("dataSources") List<ReportDataSource> dataSources);

    @Update("UPDATE report_data_source SET error_count = 0, error_message = NULL, updated_time = NOW() WHERE data_source_id = #{dataSourceId}")
    int clearErrorStatus(@Param("dataSourceId") Long dataSourceId);
}