package com.neu.Finance.service;

import com.neu.Finance.dto.StatisticsDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    // 首页概览数据（卡片）
    Map<String, Object> getOverview(Long userId, LocalDate startDate, LocalDate endDate);

    // 收支趋势（近N个月）
    List<Map<String, Object>> getTrend(Long userId, int months);

    // 支出分类占比
    List<Map<String, Object>> getExpenseCategory(Long userId, LocalDate startDate, LocalDate endDate);

    // 收入分类占比
    List<Map<String, Object>> getIncomeCategory(Long userId, LocalDate startDate, LocalDate endDate);

    // 预算执行进度
    List<Map<String, Object>> getBudgetProgress(Long userId, String yearMonth);

    // 资产分布（按资产类型或账户）
    List<Map<String, Object>> getAssetDistribution(Long userId);
}