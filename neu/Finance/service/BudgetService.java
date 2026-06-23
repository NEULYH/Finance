package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.Budget;
import com.neu.Finance.dto.BudgetSummaryDTO;
import java.util.List;

public interface BudgetService extends IService<Budget> {
    List<Budget> getUserBudgets(Long userId, String yearMonth);
    boolean setBudget(Budget budget);
    boolean updateBudget(Long id, Long userId, java.math.BigDecimal amount);
    boolean deleteBudget(Long id, Long userId);
    List<BudgetSummaryDTO> getBudgetSummary(Long userId, String yearMonth);
}