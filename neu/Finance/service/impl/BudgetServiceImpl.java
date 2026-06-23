package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.entity.Budget;
import com.neu.Finance.dto.BudgetSummaryDTO;
import com.neu.Finance.mapper.BudgetMapper;
import com.neu.Finance.service.BudgetService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class BudgetServiceImpl extends ServiceImpl<BudgetMapper, Budget> implements BudgetService {

    @Override
    public List<Budget> getUserBudgets(Long userId, String yearMonth) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, userId);
        if (yearMonth != null && !yearMonth.isEmpty()) {
            wrapper.eq(Budget::getYearMonth, yearMonth);
        }
        wrapper.orderByDesc(Budget::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean setBudget(Budget budget) {
        // 检查是否已存在相同月份相同分类的预算
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getUserId, budget.getUserId())
               .eq(Budget::getCategoryId, budget.getCategoryId())
               .eq(Budget::getYearMonth, budget.getYearMonth());
        Budget existing = this.getOne(wrapper);
        if (existing != null) {
            existing.setBudgetAmount(budget.getBudgetAmount());
            return this.updateById(existing);
        } else {
            return this.save(budget);
        }
    }

    @Override
    public boolean updateBudget(Long id, Long userId, BigDecimal amount) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getId, id).eq(Budget::getUserId, userId);
        Budget budget = this.getOne(wrapper);
        if (budget == null) return false;
        budget.setBudgetAmount(amount);
        return this.updateById(budget);
    }

    @Override
    public boolean deleteBudget(Long id, Long userId) {
        LambdaQueryWrapper<Budget> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Budget::getId, id).eq(Budget::getUserId, userId);
        return this.remove(wrapper);
    }

    @Override
    public List<BudgetSummaryDTO> getBudgetSummary(Long userId, String yearMonth) {
        List<BudgetSummaryDTO> list = baseMapper.getBudgetSummary(userId, yearMonth);
        // 计算剩余金额和百分比
        for (BudgetSummaryDTO dto : list) {
            BigDecimal actual = dto.getActualAmount() != null ? dto.getActualAmount() : BigDecimal.ZERO;
            BigDecimal budget = dto.getBudgetAmount() != null ? dto.getBudgetAmount() : BigDecimal.ZERO;
            dto.setRemaining(budget.subtract(actual));
            if (budget.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal percent = actual.divide(budget, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                dto.setPercentage(percent);
            } else {
                dto.setPercentage(BigDecimal.ZERO);
            }
        }
        return list;
    }
}