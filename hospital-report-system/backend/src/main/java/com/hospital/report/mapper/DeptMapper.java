package com.hospital.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.report.entity.Dept;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeptMapper extends BaseMapper<Dept> {

    /**
     * 根据部门代码查找部门
     */
    @Select("SELECT * FROM sys_dept WHERE dept_code = #{deptCode} AND status = 1")
    Dept findByDeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据父部门ID查找子部门
     */
    @Select("SELECT * FROM sys_dept WHERE parent_id = #{parentId} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<Dept> findByParentIdOrderBySortOrder(@Param("parentId") Long parentId);

    /**
     * 查找所有活跃部门，按排序字段和ID排序
     */
    @Select("SELECT * FROM sys_dept WHERE status = 1 ORDER BY sort_order ASC, id ASC")
    List<Dept> findActiveDepts();

    /**
     * 检查部门代码是否存在（包括非活跃状态）
     */
    @Select("SELECT COUNT(*) > 0 FROM sys_dept WHERE dept_code = #{deptCode}")
    boolean existsByDeptCode(@Param("deptCode") String deptCode);

    /**
     * 根据部门类型查找部门
     */
    @Select("SELECT * FROM sys_dept WHERE dept_type = #{deptType} AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<Dept> findByDeptType(@Param("deptType") String deptType);

    /**
     * 查找根部门（父ID为0或null的部门）
     */
    @Select("SELECT * FROM sys_dept WHERE (parent_id = 0 OR parent_id IS NULL) AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<Dept> findRootDepts();

    /**
     * 根据部门名称模糊查询
     */
    @Select("SELECT * FROM sys_dept WHERE dept_name LIKE CONCAT('%', #{deptName}, '%') AND status = 1 ORDER BY sort_order ASC, id ASC")
    List<Dept> findByDeptNameLike(@Param("deptName") String deptName);

    /**
     * 统计某个父部门下的子部门数量
     */
    @Select("SELECT COUNT(*) FROM sys_dept WHERE parent_id = #{parentId} AND status = 1")
    int countByParentId(@Param("parentId") Long parentId);
}
