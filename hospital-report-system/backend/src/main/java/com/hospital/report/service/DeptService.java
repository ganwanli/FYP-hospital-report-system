package com.hospital.report.service;

import com.hospital.report.entity.Dept;
import com.hospital.report.vo.DeptTreeVO;

import java.util.List;

public interface DeptService {

    /**
     * 获取所有部门
     */
    List<Dept> getAllDepts();

    /**
     * 获取部门树
     */
    List<DeptTreeVO> getDeptTree();

    /**
     * 根据ID获取部门
     */
    Dept getDeptById(Long id);

    /**
     * 根据代码获取部门
     */
    Dept getDeptByCode(String deptCode);

    /**
     * 创建部门
     */
    Dept createDept(Dept dept);

    /**
     * 更新部门
     */
    Dept updateDept(Dept dept);

    /**
     * 删除部门
     */
    void deleteDept(Long id);
}
