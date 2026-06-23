package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.Budget;
import com.neu.Finance.dto.BudgetSummaryDTO;
import com.neu.Finance.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @GetMapping
    public Result<List<Budget>> list(@RequestParam(required = false) String yearMonth, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<Budget> list = budgetService.getUserBudgets(userId, yearMonth);
        return Result.success(list);
    }

    @PostMapping
    public Result<String> setBudget(@RequestBody Budget budget, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        budget.setUserId(userId);
        boolean ok = budgetService.setBudget(budget);
        return ok ? Result.success("设置成功") : Result.error("设置失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateBudget(@PathVariable Long id, @RequestBody Map<String, BigDecimal> payload, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        BigDecimal amount = payload.get("budgetAmount");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("预算金额无效");
        }
        boolean ok = budgetService.updateBudget(id, userId, amount);
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteBudget(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean ok = budgetService.deleteBudget(id, userId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/summary")
    public Result<List<BudgetSummaryDTO>> summary(@RequestParam String yearMonth, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<BudgetSummaryDTO> list = budgetService.getBudgetSummary(userId, yearMonth);
        return Result.success(list);
    }
}