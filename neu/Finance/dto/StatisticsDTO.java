package com.neu.Finance.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class StatisticsDTO {
    private BigDecimal totalAsset;
    private BigDecimal monthIncome;
    private BigDecimal monthExpense;
    private BigDecimal balance;
    private List<Map<String, Object>> trend;
    private List<Map<String, Object>> expenseCategory;
    private List<Map<String, Object>> incomeCategory;
    private List<Map<String, Object>> budgetProgress;
    private List<Map<String, Object>> assetDistribution;

    // 无参构造
    public StatisticsDTO() {}

    // Getters and Setters
    public BigDecimal getTotalAsset() { return totalAsset; }
    public void setTotalAsset(BigDecimal totalAsset) { this.totalAsset = totalAsset; }

    public BigDecimal getMonthIncome() { return monthIncome; }
    public void setMonthIncome(BigDecimal monthIncome) { this.monthIncome = monthIncome; }

    public BigDecimal getMonthExpense() { return monthExpense; }
    public void setMonthExpense(BigDecimal monthExpense) { this.monthExpense = monthExpense; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public List<Map<String, Object>> getTrend() { return trend; }
    public void setTrend(List<Map<String, Object>> trend) { this.trend = trend; }

    public List<Map<String, Object>> getExpenseCategory() { return expenseCategory; }
    public void setExpenseCategory(List<Map<String, Object>> expenseCategory) { this.expenseCategory = expenseCategory; }

    public List<Map<String, Object>> getIncomeCategory() { return incomeCategory; }
    public void setIncomeCategory(List<Map<String, Object>> incomeCategory) { this.incomeCategory = incomeCategory; }

    public List<Map<String, Object>> getBudgetProgress() { return budgetProgress; }
    public void setBudgetProgress(List<Map<String, Object>> budgetProgress) { this.budgetProgress = budgetProgress; }

    public List<Map<String, Object>> getAssetDistribution() { return assetDistribution; }
    public void setAssetDistribution(List<Map<String, Object>> assetDistribution) { this.assetDistribution = assetDistribution; }
}