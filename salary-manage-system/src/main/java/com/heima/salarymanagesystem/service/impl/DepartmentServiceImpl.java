package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.Department;
import com.heima.salarymanagesystem.mapper.DepartmentMapper;
import com.heima.salarymanagesystem.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 * 支持多级组织架构（总部 → 一级部门 → 分部）的树形查询
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getDeptTree() {
        List<Department> allDepts = this.list(new LambdaQueryWrapper<Department>()
                .orderByAsc(Department::getSortOrder));

        // 找到总部（parentId=0）
        List<Map<String, Object>> tree = new ArrayList<>();
        List<Department> roots = allDepts.stream()
                .filter(d -> d.getParentId() == 0)
                .collect(Collectors.toList());

        for (Department root : roots) {
            Map<String, Object> node = buildTreeNode(root, allDepts);
            tree.add(node);
        }
        return tree;
    }

    /**
     * 递归构建部门树节点
     */
    private Map<String, Object> buildTreeNode(Department dept, List<Department> allDepts) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", dept.getId());
        node.put("deptName", dept.getDeptName());
        node.put("parentId", dept.getParentId());
        node.put("deptLevel", dept.getDeptLevel());
        node.put("managerId", dept.getManagerId());
        node.put("sortOrder", dept.getSortOrder());
        node.put("description", dept.getDescription());

        // 查找子部门（按sortOrder排序）
        List<Map<String, Object>> children = new ArrayList<>();
        List<Department> sortedChildren = allDepts.stream()
                .filter(d -> d.getParentId().equals(dept.getId()))
                .sorted((a, b) -> Integer.compare(a.getSortOrder() != null ? a.getSortOrder() : 0,
                                                   b.getSortOrder() != null ? b.getSortOrder() : 0))
                .collect(Collectors.toList());
        for (Department d : sortedChildren) {
            children.add(buildTreeNode(d, allDepts));
        }
        node.put("children", children);
        return node;
    }

    @Override
    public List<Department> getByLevel(Integer level) {
        return this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getDeptLevel, level)
                .orderByAsc(Department::getSortOrder));
    }

    @Override
    public List<Department> getChildren(Long parentId) {
        return this.list(new LambdaQueryWrapper<Department>()
                .eq(Department::getParentId, parentId)
                .orderByAsc(Department::getSortOrder));
    }

    @Override
    public List<Map<String, Object>> getDeptInfoList() {
        // 查询视图 v_department_info
        return jdbcTemplate.queryForList("SELECT * FROM v_department_info ORDER BY dept_level, sort_order");
    }
}
