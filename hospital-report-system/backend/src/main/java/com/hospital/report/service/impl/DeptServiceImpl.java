package com.hospital.report.service.impl;

import com.hospital.report.entity.Dept;
import com.hospital.report.service.DeptService;
import com.hospital.report.vo.DeptTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DeptServiceImpl implements DeptService {

    @Autowired
    private com.hospital.report.mapper.DeptMapper deptMapper;

    @Override
    public List<Dept> getAllDepts() {
        return deptMapper.findActiveDepts();
    }

    @Override
    public List<DeptTreeVO> getDeptTree() {
        List<Dept> allDepts = deptMapper.findActiveDepts();
        return buildDeptTree(allDepts, null);
    }

    @Override
    public Dept getDeptById(Long id) {
        return deptMapper.selectById(id);
    }

    @Override
    public Dept getDeptByCode(String deptCode) {
        return deptMapper.findByDeptCode(deptCode);
    }

    @Override
    public Dept createDept(Dept dept) {
        deptMapper.insert(dept);
        return dept;
    }

    @Override
    public Dept updateDept(Dept dept) {
        deptMapper.updateById(dept);
        return dept;
    }

    @Override
    public void deleteDept(Long id) {
        deptMapper.deleteById(id);
    }

    /**
     * 构建部门树
     */
    private List<DeptTreeVO> buildDeptTree(List<Dept> allDepts, Long parentId) {
        List<DeptTreeVO> result = new ArrayList<>();
        
        List<Dept> children = allDepts.stream()
                .filter(dept -> {
                    if (parentId == null) {
                        return dept.getParentId() == null || dept.getParentId() == 0;
                    } else {
                        return parentId.equals(dept.getParentId());
                    }
                })
                .collect(Collectors.toList());

        for (Dept dept : children) {
            DeptTreeVO treeVO = new DeptTreeVO();
            BeanUtils.copyProperties(dept, treeVO);
            
            // 递归构建子树
            List<DeptTreeVO> childrenTree = buildDeptTree(allDepts, dept.getId());
            treeVO.setChildren(childrenTree);
            
            result.add(treeVO);
        }
        
        return result;
    }
}
