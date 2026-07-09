package com.heima.salarymanagesystem.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.salarymanagesystem.entity.MonthlySalary;
import com.heima.salarymanagesystem.mapper.MonthlySalaryMapper;
import com.heima.salarymanagesystem.service.MonthlySalaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 月度工资服务实现
 * 核心薪资计算通过数据库存储过程实现，确保数据一致性和计算性能
 */
@Service
@RequiredArgsConstructor
public class MonthlySalaryServiceImpl extends ServiceImpl<MonthlySalaryMapper, MonthlySalary>
        implements MonthlySalaryService {

    @Override
    public void calculateMonthlySalary(Integer year, Integer month) {
        baseMapper.callCalcMonthlySalary(year, month);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmSalary(Integer year, Integer month) {
        baseMapper.callConfirmSalary(year, month);
    }

    @Override
    public List<Map<String, Object>> queryEmployeeSalary(Long employeeId, Integer year, Integer month) {
        return baseMapper.callQueryEmployeeSalary(employeeId, year, month);
    }

    @Override
    public List<Map<String, Object>> queryDepartmentSalary(Long departmentId, Integer year, Integer month) {
        return baseMapper.callQueryDepartmentSalary(departmentId, year, month);
    }

    @Override
    public List<Map<String, Object>> salaryStructureReport(Integer year, Integer month) {
        return baseMapper.callSalaryStructureReport(year, month);
    }

    @Override
    public List<Map<String, Object>> departmentSalaryStats(Integer year, Integer month) {
        return baseMapper.callDepartmentSalaryStats(year, month);
    }

    @Override
    public List<Map<String, Object>> employeeAnnualStats(Long employeeId, Integer year) {
        return baseMapper.callEmployeeAnnualStats(employeeId, year);
    }

    @Override
    public List<Map<String, Object>> salaryQuery(Integer year, Integer month, Long deptId,
                                                   String employeeName, String empNo) {
        return baseMapper.callSalaryQuery(year, month, deptId, employeeName, empNo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateManualItems(Long salaryId, Map<String, Object> manualItems) {
        MonthlySalary salary = this.getById(salaryId);
        if (salary == null) {
            throw new RuntimeException("工资记录不存在");
        }
        if ("CONFIRMED".equals(salary.getStatus())) {
            throw new RuntimeException("工资已确认，无法修改");
        }

        // 只允许修改手动录入项
        if (manualItems.containsKey("projectBonus")) {
            salary.setProjectBonus(new BigDecimal(manualItems.get("projectBonus").toString()));
        }
        if (manualItems.containsKey("taxDeduction")) {
            salary.setTaxDeduction(new BigDecimal(manualItems.get("taxDeduction").toString()));
        }
        if (manualItems.containsKey("otherAdditions")) {
            salary.setOtherAdditions(new BigDecimal(manualItems.get("otherAdditions").toString()));
        }
        if (manualItems.containsKey("otherDeductions")) {
            salary.setOtherDeductions(new BigDecimal(manualItems.get("otherDeductions").toString()));
        }

        // 重新计算汇总
        BigDecimal grossSalary = salary.getBaseSalary()
                .add(salary.getPositionSalary())
                .add(salary.getTitleSalary())
                .add(salary.getSeniorityPay())
                .add(salary.getFullAttendanceBonus())
                .add(salary.getProjectBonus())
                .add(salary.getOtherAdditions());
        salary.setGrossSalary(grossSalary);

        BigDecimal totalDeduction = salary.getLeaveDeduction()
                .add(salary.getLateDeduction())
                .add(salary.getAbsentDeduction())
                .add(salary.getTaxDeduction())
                .add(salary.getOtherDeductions());
        salary.setTotalDeduction(totalDeduction);

        salary.setNetSalary(grossSalary.subtract(totalDeduction));

        this.updateById(salary);
    }
}
