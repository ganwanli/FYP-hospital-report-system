package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.SyncTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface SyncTaskMapper extends BaseMapper<SyncTask> {

    @Select("SELECT * FROM sync_task WHERE is_enabled = 1 AND is_deleted = 0")
    List<SyncTask> selectEnabledTasks();

    @Select("SELECT * FROM sync_task WHERE task_type = #{taskType} AND is_enabled = 1 AND is_deleted = 0")
    List<SyncTask> selectTasksByType(@Param("taskType") String taskType);

    @Update("UPDATE sync_task SET last_sync_value = #{lastSyncValue}, updated_time = NOW() WHERE id = #{id}")
    int updateLastSyncValue(@Param("id") Long id, @Param("lastSyncValue") String lastSyncValue);

    @Update("UPDATE sync_task SET status = #{status}, updated_time = NOW() WHERE id = #{id}")
    int updateTaskStatus(@Param("id") Long id, @Param("status") Integer status);

    @Select("SELECT " +
            "  st.*, " +
            "  ds_source.datasource_name as source_datasource_name, " +
            "  ds_target.datasource_name as target_datasource_name " +
            "FROM sync_task st " +
            "LEFT JOIN datasource ds_source ON st.source_datasource_id = ds_source.id " +
            "LEFT JOIN datasource ds_target ON st.target_datasource_id = ds_target.id " +
            "WHERE st.is_deleted = 0 " +
            "ORDER BY st.created_time DESC")
    List<Map<String, Object>> selectTasksWithDataSourceNames();

    @Select("SELECT " +
            "  COUNT(*) as total_tasks, " +
            "  SUM(CASE WHEN is_enabled = 1 THEN 1 ELSE 0 END) as enabled_tasks, " +
            "  SUM(CASE WHEN sync_type = 'TABLE' THEN 1 ELSE 0 END) as table_sync_tasks, " +
            "  SUM(CASE WHEN sync_type = 'SQL' THEN 1 ELSE 0 END) as sql_sync_tasks " +
            "FROM sync_task " +
            "WHERE is_deleted = 0")
    Map<String, Object> getTaskStatistics();
}