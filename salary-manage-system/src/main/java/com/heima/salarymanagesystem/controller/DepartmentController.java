package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.Department;
import com.heima.salarymanagesystem.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 部门管理控制器
 * 支持多级组织架构（总部 → 一级部门 → 分部）的 CRUD 和树形查询
 */
@RestController
@RequestMapping("/api/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 获取部门树结构
     * GET /api/department/tree
     */
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> getDeptTree() {
        return Result.ok(departmentService.getDeptTree());
    }

    /**
     * 获取部门完整信息列表（含负责人、员工数，来自视图）
     * GET /api/department/info
     */
    @GetMapping("/info")
    public Result<List<Map<String, Object>>> getDeptInfoList() {
        return Result.ok(departmentService.getDeptInfoList());
    }

    /**
     * 查询所有部门
     * GET /api/department
     */
    @GetMapping
    public Result<List<Department>> listAll() {
        return Result.ok(departmentService.list());
    }

    /**
     * 根据层级查询部门
     * GET /api/department/level/{level}
     */
    @GetMapping("/level/{level}")
    public Result<List<Department>> getByLevel(@PathVariable Integer level) {
        return Result.ok(departmentService.getByLevel(level));
    }

    /**
     * 查询子部门
     * GET /api/department/children/{parentId}
     */
    @GetMapping("/children/{parentId}")
    public Result<List<Department>> getChildren(@PathVariable Long parentId) {
        return Result.ok(departmentService.getChildren(parentId));
    }

    /**
     * 根据ID查询部门
     * GET /api/department/{id}
     */
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        return Result.ok(departmentService.getById(id));
    }

    /**
     * 新增部门
     * POST /api/department
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody Department department) {
        return Result.ok(departmentService.save(department));
    }

    /**
     * 更新部门
     * PUT /api/department
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody Department department) {
        return Result.ok(departmentService.updateById(department));
    }

    /**
     * 删除部门
     * DELETE /api/department/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(departmentService.removeById(id));
    }
}
