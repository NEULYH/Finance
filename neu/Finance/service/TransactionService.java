package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface TransactionService extends IService<Transaction> {
    // 分页条件查询
    List<Transaction> getList(Long userId, String type, Long categoryId, Long accountId,
                              LocalDateTime startDate, LocalDateTime endDate, String keyword,
                              int pageNo, int pageSize, long total);

    // 统计卡片数据（总收入、总支出、结余）
    Map<String, BigDecimal> getStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 月度趋势（返回每月收支数据）
    List<Map<String, Object>> getMonthlyTrend(Long userId, LocalDateTime startDate);

    // 支出分类占比
    List<Map<String, Object>> getExpenseCategory(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    // 添加交易并自动更新账户余额
    boolean addTransaction(Transaction transaction);
    Map<String, Object> getListWithNames(Long userId, String type, Long categoryId, Long accountId,
            LocalDateTime startDate, LocalDateTime endDate, String keyword,
            int pageNo, int pageSize);
}