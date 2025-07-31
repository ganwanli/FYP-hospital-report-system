package com.hospital.report.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.ai.entity.SqlAnalysisLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SqlAnalysisLogMapper extends BaseMapper<SqlAnalysisLog> {
    
    @Select("SELECT COUNT(*) FROM sql_analysis_log WHERE user_id = #{userId} AND analysis_type = #{analysisType}")
    Long countByUserIdAndAnalysisType(@Param("userId") Long userId, @Param("analysisType") String analysisType);
}