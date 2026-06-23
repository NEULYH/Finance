package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neu.Finance.dto.MonthlyReportDTO;
import com.neu.Finance.entity.Category;
import com.neu.Finance.entity.Transaction;
import com.neu.Finance.mapper.CategoryMapper;
import com.neu.Finance.mapper.TransactionMapper;
import com.neu.Finance.service.AiReportService;
import com.neu.Finance.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private AiReportService aiReportService;

    @Override
    public MonthlyReportDTO generateMonthlyReport(Long userId, String yearMonth) {
        MonthlyReportDTO dto = new MonthlyReportDTO();
        try {
            // 1. 解析年月
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            LocalDate startLocal = LocalDate.parse(yearMonth + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            LocalDateTime startDateTime = startLocal.atStartOfDay();
            LocalDateTime endDateTime = startLocal.plusMonths(1).atStartOfDay().minusSeconds(1);

            // 2. 查询当月所有交易
            LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Transaction::getUserId, userId)
                    .between(Transaction::getDate, startDateTime, endDateTime);
            List<Transaction> transactions = transactionMapper.selectList(wrapper);

            // 3. 计算收支汇总
            BigDecimal totalIncome = BigDecimal.ZERO;
            BigDecimal totalExpense = BigDecimal.ZERO;
            for (Transaction t : transactions) {
                if ("income".equals(t.getType())) {
                    totalIncome = totalIncome.add(t.getAmount());
                } else if ("expense".equals(t.getType())) {
                    totalExpense = totalExpense.add(t.getAmount());
                }
            }
            BigDecimal surplus = totalIncome.subtract(totalExpense);
            BigDecimal surplusRate = BigDecimal.ZERO;
            if (totalIncome.compareTo(BigDecimal.ZERO) != 0) {
                surplusRate = surplus.divide(totalIncome, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            }

            // 4. 获取所有分类名称映射
            LambdaQueryWrapper<Category> catWrapper = new LambdaQueryWrapper<>();
            catWrapper.eq(Category::getUserId, userId);
            List<Category> categories = categoryMapper.selectList(catWrapper);
            Map<Long, String> categoryNameMap = categories.stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));

            // 5. 支出排行
            Map<Long, BigDecimal> expenseMap = new HashMap<>();
            for (Transaction t : transactions) {
                if ("expense".equals(t.getType()) && t.getCategoryId() != null) {
                    expenseMap.merge(t.getCategoryId(), t.getAmount(), BigDecimal::add);
                }
            }
            List<Map<String, Object>> expenseRanking = expenseMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("categoryId", e.getKey());
                        item.put("amount", e.getValue());
                        item.put("categoryName", categoryNameMap.getOrDefault(e.getKey(), "未知"));
                        return item;
                    })
                    .collect(Collectors.toList());

            // 6. 收入排行
            Map<Long, BigDecimal> incomeMap = new HashMap<>();
            for (Transaction t : transactions) {
                if ("income".equals(t.getType()) && t.getCategoryId() != null) {
                    incomeMap.merge(t.getCategoryId(), t.getAmount(), BigDecimal::add);
                }
            }
            List<Map<String, Object>> incomeRanking = incomeMap.entrySet().stream()
                    .sorted(Map.Entry.<Long, BigDecimal>comparingByValue().reversed())
                    .limit(5)
                    .map(e -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("categoryId", e.getKey());
                        item.put("amount", e.getValue());
                        item.put("categoryName", categoryNameMap.getOrDefault(e.getKey(), "未知"));
                        return item;
                    })
                    .collect(Collectors.toList());

            // 7. 填充 DTO
            dto.setYearMonth(yearMonth);
            dto.setTotalIncome(totalIncome);
            dto.setTotalExpense(totalExpense);
            dto.setSurplus(surplus);
            dto.setSurplusRate(surplusRate);
            dto.setExpenseRanking(expenseRanking);
            dto.setIncomeRanking(incomeRanking);
            dto.setAiInsight("正在生成智能洞察...");
            dto.setSuggestions(new ArrayList<>());

            // 8. 调用 AI 丰富内容（即使失败也会回退）
            aiReportService.enrichWithAi(dto);

        } catch (Exception e) {
            log.error("生成月度报告失败", e);
            // 返回一个默认报告，避免前端 500
            dto.setYearMonth(yearMonth);
            dto.setTotalIncome(BigDecimal.ZERO);
            dto.setTotalExpense(BigDecimal.ZERO);
            dto.setSurplus(BigDecimal.ZERO);
            dto.setSurplusRate(BigDecimal.ZERO);
            dto.setExpenseRanking(new ArrayList<>());
            dto.setIncomeRanking(new ArrayList<>());
            dto.setAiInsight("报告生成失败，请稍后重试。");
            dto.setSuggestions(Arrays.asList("检查网络或联系管理员"));
        }
        return dto;
    }
}