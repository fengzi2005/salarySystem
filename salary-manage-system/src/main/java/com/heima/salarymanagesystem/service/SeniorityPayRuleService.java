package com.heima.salarymanagesystem.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.salarymanagesystem.entity.SeniorityPayRule;

/**
 * 工龄工资规则服务接口
 */
public interface SeniorityPayRuleService extends IService<SeniorityPayRule> {

    /**
     * 启用规则（同一时间只有一个规则启用）
     * 启用新规则时会自动停用旧规则
     */
    void enableRule(Long ruleId);

    /**
     * 获取当前生效的规则
     */
    SeniorityPayRule getActiveRule();
}
