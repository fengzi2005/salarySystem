package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.SeniorityPayRule;
import com.heima.salarymanagesystem.mapper.SeniorityPayRuleMapper;
import com.heima.salarymanagesystem.service.SeniorityPayRuleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 工龄工资规则服务实现
 * 支持新旧规则的无缝切换
 */
@Service
public class SeniorityPayRuleServiceImpl extends ServiceImpl<SeniorityPayRuleMapper, SeniorityPayRule>
        implements SeniorityPayRuleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableRule(Long ruleId) {
        // 停用所有规则
        LambdaUpdateWrapper<SeniorityPayRule> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(SeniorityPayRule::getIsActive, 0)
                     .eq(SeniorityPayRule::getIsActive, 1);
        this.update(updateWrapper);

        // 启用指定规则
        SeniorityPayRule rule = this.getById(ruleId);
        if (rule != null) {
            rule.setIsActive(1);
            this.updateById(rule);
        }
    }

    @Override
    public SeniorityPayRule getActiveRule() {
        return this.getOne(new LambdaQueryWrapper<SeniorityPayRule>()
                .eq(SeniorityPayRule::getIsActive, 1));
    }
}
