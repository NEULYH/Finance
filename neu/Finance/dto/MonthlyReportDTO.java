package com.neu.Finance.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MonthlyReportDTO {
    private String yearMonth;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal surplus;
    private BigDecimal surplusRate;
    private List<Map<String, Object>> expenseRanking;
    private List<Map<String, Object>> incomeRanking;
    private String aiInsight;
    private List<String> suggestions;

    public MonthlyReportDTO() {}

    // Getters and Setters
    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }

    public BigDecimal getTotalIncome() { return totalIncome; }
    public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }

    public BigDecimal getTotalExpense() { return totalExpense; }
    public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }

    public BigDecimal getSurplus() { return surplus; }
    public void setSurplus(BigDecimal surplus) { this.surplus = surplus; }

    public BigDecimal getSurplusRate() { return surplusRate; }
    public void setSurplusRate(BigDecimal surplusRate) { this.surplusRate = surplusRate; }

    public List<Map<String, Object>> getExpenseRanking() { return expenseRanking; }
    public void setExpenseRanking(List<Map<String, Object>> expenseRanking) { this.expenseRanking = expenseRanking; }

    public List<Map<String, Object>> getIncomeRanking() { return incomeRanking; }
    public void setIncomeRanking(List<Map<String, Object>> incomeRanking) { this.incomeRanking = incomeRanking; }

    public String getAiInsight() { return aiInsight; }
    public void setAiInsight(String aiInsight) { this.aiInsight = aiInsight; }

    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
}