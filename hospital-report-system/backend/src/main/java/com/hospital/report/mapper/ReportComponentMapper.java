package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.ReportComponent;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReportComponentMapper extends BaseMapper<ReportComponent> {

    @Select("SELECT * FROM report_component WHERE report_id = #{reportId} ORDER BY component_order ASC, z_index ASC")
    List<ReportComponent> selectByReportId(@Param("reportId") Long reportId);

    @Delete("DELETE FROM report_component WHERE report_id = #{reportId}")
    int deleteByReportId(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_component WHERE component_id = #{componentId}")
    ReportComponent selectByComponentId(@Param("componentId") Long componentId);

    @Update("UPDATE report_component SET " +
            "position_x = #{positionX}, " +
            "position_y = #{positionY}, " +
            "width = #{width}, " +
            "height = #{height}, " +
            "updated_time = NOW() " +
            "WHERE component_id = #{componentId}")
    int updateComponentPosition(@Param("componentId") Long componentId,
                               @Param("positionX") Integer positionX,
                               @Param("positionY") Integer positionY,
                               @Param("width") Integer width,
                               @Param("height") Integer height);

    @Update("UPDATE report_component SET z_index = #{zIndex}, updated_time = NOW() WHERE component_id = #{componentId}")
    int updateComponentZIndex(@Param("componentId") Long componentId, @Param("zIndex") Integer zIndex);

    @Update("UPDATE report_component SET is_visible = #{isVisible}, updated_time = NOW() WHERE component_id = #{componentId}")
    int updateComponentVisibility(@Param("componentId") Long componentId, @Param("isVisible") Boolean isVisible);

    @Update("UPDATE report_component SET is_locked = #{isLocked}, updated_time = NOW() WHERE component_id = #{componentId}")
    int updateComponentLock(@Param("componentId") Long componentId, @Param("isLocked") Boolean isLocked);

    @Select("SELECT MAX(z_index) FROM report_component WHERE report_id = #{reportId}")
    Integer selectMaxZIndex(@Param("reportId") Long reportId);

    @Select("SELECT MAX(component_order) FROM report_component WHERE report_id = #{reportId}")
    Integer selectMaxComponentOrder(@Param("reportId") Long reportId);

    @Select("SELECT * FROM report_component WHERE parent_component_id = #{parentComponentId} ORDER BY component_order ASC")
    List<ReportComponent> selectByParentComponentId(@Param("parentComponentId") Long parentComponentId);

    @Select("SELECT COUNT(*) FROM report_component WHERE report_id = #{reportId}")
    int countByReportId(@Param("reportId") Long reportId);

    @Insert("<script>" +
            "INSERT INTO report_component " +
            "(report_id, component_type, component_name, position_x, position_y, width, height, z_index, " +
            "data_source_id, data_config, style_config, chart_config, table_config, text_config, image_config, " +
            "is_visible, is_locked, component_order, parent_component_id, created_time, updated_time, " +
            "conditions_config, interaction_config) " +
            "VALUES " +
            "<foreach collection='components' item='comp' separator=','>" +
            "(#{comp.reportId}, #{comp.componentType}, #{comp.componentName}, #{comp.positionX}, #{comp.positionY}, " +
            "#{comp.width}, #{comp.height}, #{comp.zIndex}, #{comp.dataSourceId}, #{comp.dataConfig}, " +
            "#{comp.styleConfig}, #{comp.chartConfig}, #{comp.tableConfig}, #{comp.textConfig}, #{comp.imageConfig}, " +
            "#{comp.isVisible}, #{comp.isLocked}, #{comp.componentOrder}, #{comp.parentComponentId}, " +
            "#{comp.createdTime}, #{comp.updatedTime}, #{comp.conditionsConfig}, #{comp.interactionConfig})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("components") List<ReportComponent> components);

    @Update("UPDATE report_component SET component_order = #{componentOrder}, updated_time = NOW() WHERE component_id = #{componentId}")
    int updateComponentOrder(@Param("componentId") Long componentId, @Param("componentOrder") Integer componentOrder);

    @Select("SELECT component_type, COUNT(*) as count FROM report_component WHERE report_id = #{reportId} GROUP BY component_type")
    List<java.util.Map<String, Object>> selectComponentTypeStatistics(@Param("reportId") Long reportId);
}