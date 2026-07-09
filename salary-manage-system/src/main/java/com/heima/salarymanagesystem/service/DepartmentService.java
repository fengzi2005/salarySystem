package com.heima.salarymanagesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.salarymanagesystem.entity.Department;

import java.util.List;
import java.util.Map;

/**
 * 部门服务接口
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 获取部门树结构（多级组织架构）
     * @return 部门树列表
     */
    List<Map<String, Object>> getDeptTree();

    /**
     * 根据层级查询部门
     * @param level 层级：1-总部, 2-一级部门, 3-分部
     */
    List<Department> getByLevel(Integer level);

    /**
     * 查询子部门（根据父部门ID）
     */
    List<Department> getChildren(Long parentId);

    /**
     * 获取部门完整信息（含负责人、下属员工数）
     */
    List<Map<String, Object>> getDeptInfoList();
}
