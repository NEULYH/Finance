package com.neu.Finance.service.impl;

import com.neu.Finance.mapper.TransactionMapper;
import com.neu.Finance.dto.BudgetSummaryDTO;
import com.neu.Finance.mapper.AccountMapper;
import com.neu.Finance.mapper.AssetMapper;
import com.neu.Finance.mapper.BudgetMapper;
import com.neu.Finance.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private AssetMapper assetMapper;

    @Autowired
    private BudgetMapper budgetMapper;

    @Override
    public Map<String, Object> getOverview(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        Map<String, BigDecimal> stats = transactionMapper.getMonthStatistics(userId, start, end);
        if (stats == null) {
            stats = new HashMap<>();
            stats.put("totalIncome", BigDecimal.ZERO);
            stats.put("totalExpense", BigDecimal.ZERO);
        }
        BigDecimal monthIncome = stats.getOrDefault("totalIncome", BigDecimal.ZERO);
        BigDecimal monthExpense = stats.getOrDefault("totalExpense", BigDecimal.ZERO);
        BigDecimal balance = monthIncome.subtract(monthExpense);

        // 总资产 = 所有账户余额之和 + 资产市值之和
        BigDecimal totalAccountBalance = accountMapper.getTotalBalance(userId);
        if (totalAccountBalance == null) totalAccountBalance = BigDecimal.ZERO;
        Map<String, BigDecimal> assetSummary = assetMapper.getAssetSummary(userId);
        BigDecimal totalAssetValue = assetSummary != null ? assetSummary.getOrDefault("totalMarketValue", BigDecimal.ZERO) : BigDecimal.ZERO;
        BigDecimal totalAsset = totalAccountBalance.add(totalAssetValue);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAsset", totalAsset);
        result.put("monthIncome", monthIncome);
        result.put("monthExpense", monthExpense);
        result.put("balance", balance);
        return result;
    }

    @Override
    public List<Map<String, Object>> getTrend(Long userId, int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months - 1).withDayOfMonth(1);
        List<Map<String, Object>> trend = transactionMapper.getMonthlyTrend(userId, startDate);
        // 补齐缺失月份
        List<Map<String, Object>> fullTrend = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate current = LocalDate.now().minusMonths(months - 1).withDayOfMonth(1);
        for (int i = 0; i < months; i++) {
            String month = current.format(formatter);
            Map<String, Object> monthData = trend.stream()
                    .filter(m -> month.equals(m.get("month")))
                    .findFirst()
                    .orElse(new HashMap<>());
            Map<String, Object> item = new HashMap<>();
            item.put("month", month);
            item.put("income", monthData.getOrDefault("income", BigDecimal.ZERO));
            item.put("expense", monthData.getOrDefault("expense", BigDecimal.ZERO));
            fullTrend.add(item);
            current = current.plusMonths(1);
        }
        return fullTrend;
    }

    @Override
    public List<Map<String, Object>> getExpenseCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        return transactionMapper.getExpenseCategoryStats(userId, start, end);
    }

    @Override
    public List<Map<String, Object>> getIncomeCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        // 类似支出分类，需要单独写SQL，这里简化：复用同一个方法但查询收入类型
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        // 由于Mapper中没有收入分类查询，我们直接手写SQL（可以在Mapper中添加）
        // 这里演示：调用一个新方法（假设已添加）
        return transactionMapper.getIncomeCategoryStats(userId, start, end);
    }

    @Override
    public List<Map<String, Object>> getBudgetProgress(Long userId, String yearMonth) {
        // 使用之前BudgetMapper中的getBudgetSummary，转换成前端需要的格式
        List<BudgetSummaryDTO> budgetList = budgetMapper.getBudgetSummary(userId, yearMonth);
        List<Map<String, Object>> progress = new ArrayList<>();
        for (BudgetSummaryDTO dto : budgetList) {
            Map<String, Object> item = new HashMap<>();
            item.put("categoryName", dto.getCategoryName());
            item.put("budgetAmount", dto.getBudgetAmount());
            item.put("actualAmount", dto.getActualAmount());
            item.put("percentage", dto.getPercentage());
            progress.add(item);
        }
        return progress;
    }

    @Override
    public List<Map<String, Object>> getAssetDistribution(Long userId) {
        // 按资产类型分组统计市值
        // 需要AssetMapper提供方法，这里简化：直接从资产列表分组计算
        // 实际生产中应写SQL，这里作为示例
        // 我们假设已有方法 assetMapper.getAssetGroupByType(userId)
        return assetMapper.getAssetGroupByType(userId);
    }
}