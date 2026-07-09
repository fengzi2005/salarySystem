package com.heima.salarymanagesystem.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.SalaryItemConfig;
import com.heima.salarymanagesystem.mapper.SalaryItemConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工资项配置控制器
 * 管理系统工资项的类型、启用/停用状态
 */
@RestController
@RequestMapping("/api/salary-item")
@RequiredArgsConstructor
public class SalaryItemConfigController {

    private final SalaryItemConfigMapper salaryItemConfigMapper;

    /**
     * 查询所有工资项
     * GET /api/salary-item
     */
    @GetMapping
    public Result<List<SalaryItemConfig>> listAll() {
        List<SalaryItemConfig> list = salaryItemConfigMapper.selectList(
                new LambdaQueryWrapper<SalaryItemConfig>()
                        .orderByAsc(SalaryItemConfig::getSortOrder));
        return Result.ok(list);
    }

    /**
     * 按类型查询（SYSTEM/MANUAL）
     * GET /api/salary-item/type/{itemType}
     */
    @GetMapping("/type/{itemType}")
    public Result<List<SalaryItemConfig>> getByType(@PathVariable String itemType) {
        List<SalaryItemConfig> list = salaryItemConfigMapper.selectList(
                new LambdaQueryWrapper<SalaryItemConfig>()
                        .eq(SalaryItemConfig::getItemType, itemType)
                        .orderByAsc(SalaryItemConfig::getSortOrder));
        return Result.ok(list);
    }

    /**
     * 根据ID查询
     * GET /api/salary-item/{id}
     */
    @GetMapping("/{id}")
    public Result<SalaryItemConfig> getById(@PathVariable Long id) {
        return Result.ok(salaryItemConfigMapper.selectById(id));
    }

    /**
     * 新增工资项
     * POST /api/salary-item
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody SalaryItemConfig item) {
        return Result.ok(salaryItemConfigMapper.insert(item) > 0);
    }

    /**
     * 更新工资项
     * PUT /api/salary-item
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody SalaryItemConfig item) {
        return Result.ok(salaryItemConfigMapper.updateById(item) > 0);
    }

    /**
     * 删除工资项
     * DELETE /api/salary-item/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(salaryItemConfigMapper.deleteById(id) > 0);
    }
}
