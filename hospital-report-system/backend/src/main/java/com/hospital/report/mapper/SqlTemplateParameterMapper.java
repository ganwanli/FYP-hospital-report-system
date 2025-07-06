package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.SqlTemplateParameter;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SqlTemplateParameterMapper extends BaseMapper<SqlTemplateParameter> {

    @Select("SELECT * FROM sql_template_parameter WHERE template_id = #{templateId} ORDER BY parameter_order ASC")
    List<SqlTemplateParameter> selectByTemplateId(@Param("templateId") Long templateId);

    @Delete("DELETE FROM sql_template_parameter WHERE template_id = #{templateId}")
    int deleteByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template_parameter WHERE template_id = #{templateId} AND parameter_name = #{parameterName}")
    SqlTemplateParameter selectByTemplateIdAndName(@Param("templateId") Long templateId, @Param("parameterName") String parameterName);

    @Select("SELECT COUNT(*) FROM sql_template_parameter WHERE template_id = #{templateId}")
    int countByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT parameter_name FROM sql_template_parameter WHERE template_id = #{templateId} ORDER BY parameter_order ASC")
    List<String> selectParameterNamesByTemplateId(@Param("templateId") Long templateId);

    @Select("SELECT * FROM sql_template_parameter WHERE template_id = #{templateId} AND is_required = true ORDER BY parameter_order ASC")
    List<SqlTemplateParameter> selectRequiredParametersByTemplateId(@Param("templateId") Long templateId);

    @Update("UPDATE sql_template_parameter SET parameter_order = #{parameterOrder} WHERE parameter_id = #{parameterId}")
    int updateParameterOrder(@Param("parameterId") Long parameterId, @Param("parameterOrder") Integer parameterOrder);

    @Insert("<script>" +
            "INSERT INTO sql_template_parameter " +
            "(template_id, parameter_name, parameter_type, parameter_description, default_value, is_required, " +
            "validation_rule, validation_message, parameter_order, min_length, max_length, min_value, max_value, " +
            "allowed_values, input_type, created_time, updated_time, is_sensitive, mask_pattern) " +
            "VALUES " +
            "<foreach collection='parameters' item='param' separator=','>" +
            "(#{param.templateId}, #{param.parameterName}, #{param.parameterType}, #{param.parameterDescription}, " +
            "#{param.defaultValue}, #{param.isRequired}, #{param.validationRule}, #{param.validationMessage}, " +
            "#{param.parameterOrder}, #{param.minLength}, #{param.maxLength}, #{param.minValue}, #{param.maxValue}, " +
            "#{param.allowedValues}, #{param.inputType}, #{param.createdTime}, #{param.updatedTime}, " +
            "#{param.isSensitive}, #{param.maskPattern})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("parameters") List<SqlTemplateParameter> parameters);

    @Select("SELECT DISTINCT parameter_type FROM sql_template_parameter ORDER BY parameter_type")
    List<String> selectAllParameterTypes();

    @Select("SELECT DISTINCT input_type FROM sql_template_parameter ORDER BY input_type")
    List<String> selectAllInputTypes();
}