package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.Transaction;
import com.neu.Finance.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public Result<Map<String, Object>> getList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        // 将空字符串转为 null
        if (type != null && type.trim().isEmpty()) type = null;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        // 将 LocalDate 转换为 LocalDateTime 范围
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Map<String, Object> result = transactionService.getListWithNames(userId, type, categoryId, accountId,
                startDateTime, endDateTime, keyword, pageNo, pageSize);
        return Result.success(result);
    }

    @PostMapping
    public Result<String> add(@RequestBody Transaction transaction, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        transaction.setUserId(userId);
        boolean ok = transactionService.addTransaction(transaction);
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Transaction transaction, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        transaction.setId(id);
        transaction.setUserId(userId);
        boolean ok = transactionService.updateById(transaction);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        boolean ok = transactionService.removeById(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/statistics")
    public Result<Map<String, BigDecimal>> getStatistics(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        Map<String, BigDecimal> stats = transactionService.getStatistics(userId, start, end);
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("monthIncome", stats.getOrDefault("totalIncome", BigDecimal.ZERO));
        result.put("monthExpense", stats.getOrDefault("totalExpense", BigDecimal.ZERO));
        result.put("balance", stats.getOrDefault("balance", BigDecimal.ZERO));
        return Result.success(result);
    }

    @GetMapping("/trend")
    public Result<Map<String, List<BigDecimal>>> getTrend(@RequestParam int months, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(months - 1).withDayOfMonth(1);
        LocalDateTime start = startDate.atStartOfDay();

        List<Map<String, Object>> trendData = transactionService.getMonthlyTrend(userId, start);

        List<BigDecimal> incomeList = new ArrayList<>();
        List<BigDecimal> expenseList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 0; i < months; i++) {
            String month = startDate.plusMonths(i).format(formatter);
            Map<String, Object> monthData = trendData.stream()
                    .filter(m -> month.equals(m.get("month")))
                    .findFirst()
                    .orElse(null);
            incomeList.add(monthData != null ? (BigDecimal) monthData.get("income") : BigDecimal.ZERO);
            expenseList.add(monthData != null ? (BigDecimal) monthData.get("expense") : BigDecimal.ZERO);
        }

        Map<String, List<BigDecimal>> result = new HashMap<>();
        result.put("incomeList", incomeList);
        result.put("expenseList", expenseList);
        return Result.success(result);
    }

    @GetMapping("/expense-category")
    public Result<List<Map<String, Object>>> getExpenseCategory(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);
        List<Map<String, Object>> data = transactionService.getExpenseCategory(userId, start, end);
        return Result.success(data);
    }
}