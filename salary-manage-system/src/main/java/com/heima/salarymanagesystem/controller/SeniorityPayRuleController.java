package com.heima.salarymanagesystem.controller;

import com.heima.salarymanagesystem.common.Result;
import com.heima.salarymanagesystem.entity.SeniorityPayRule;
import com.heima.salarymanagesystem.service.SeniorityPayRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工龄工资规则控制器
 * 支持新旧规则的切换管理
 */
@RestController
@RequestMapping("/api/seniority-rule")
@RequiredArgsConstructor
public class SeniorityPayRuleController {

    private final SeniorityPayRuleService seniorityPayRuleService;

    /**
     * 查询所有规则
     * GET /api/seniority-rule
     */
    @GetMapping
    public Result<List<SeniorityPayRule>> listAll() {
        return Result.ok(seniorityPayRuleService.list());
    }

    /**
     * 获取当前生效的规则
     * GET /api/seniority-rule/active
     */
    @GetMapping("/active")
    public Result<SeniorityPayRule> getActive() {
        return Result.ok(seniorityPayRuleService.getActiveRule());
    }

    /**
     * 根据ID查询
     * GET /api/seniority-rule/{id}
     */
    @GetMapping("/{id}")
    public Result<SeniorityPayRule> getById(@PathVariable Long id) {
        return Result.ok(seniorityPayRuleService.getById(id));
    }

    /**
     * 新增规则
     * POST /api/seniority-rule
     */
    @PostMapping
    public Result<Boolean> add(@RequestBody SeniorityPayRule rule) {
        return Result.ok(seniorityPayRuleService.save(rule));
    }

    /**
     * 更新规则
     * PUT /api/seniority-rule
     */
    @PutMapping
    public Result<Boolean> update(@RequestBody SeniorityPayRule rule) {
        return Result.ok(seniorityPayRuleService.updateById(rule));
    }

    /**
     * 启用规则（自动停用其他规则，确保同时只有一个生效）
     * PUT /api/seniority-rule/{id}/enable
     */
    @PutMapping("/{id}/enable")
    public Result<Void> enableRule(@PathVariable Long id) {
        seniorityPayRuleService.enableRule(id);
        return Result.ok("规则已启用，工龄工资计算将自动使用新规则", null);
    }

    /**
     * 删除规则
     * DELETE /api/seniority-rule/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.ok(seniorityPayRuleService.removeById(id));
    }
}
