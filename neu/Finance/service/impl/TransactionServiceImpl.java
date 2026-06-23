package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.dto.TransactionVO;
import com.neu.Finance.entity.Account;
import com.neu.Finance.entity.Transaction;
import com.neu.Finance.mapper.AccountMapper;
import com.neu.Finance.mapper.TransactionMapper;
import com.neu.Finance.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, Transaction> implements TransactionService {

    @Autowired
    private AccountMapper accountMapper;

    @Override
    public List<Transaction> getList(Long userId, String type, Long categoryId, Long accountId,
                                     LocalDateTime startDate, LocalDateTime endDate, String keyword,
                                     int pageNo, int pageSize, long total) {
        LambdaQueryWrapper<Transaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Transaction::getUserId, userId);
        if (StringUtils.hasText(type)) wrapper.eq(Transaction::getType, type);
        if (categoryId != null) wrapper.eq(Transaction::getCategoryId, categoryId);
        if (accountId != null) wrapper.eq(Transaction::getAccountId, accountId);
        if (startDate != null) wrapper.ge(Transaction::getDate, startDate);
        if (endDate != null) wrapper.le(Transaction::getDate, endDate);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Transaction::getDescription, keyword)
                    .or().like(Transaction::getRemark, keyword));
        }
        wrapper.orderByDesc(Transaction::getDate);
        IPage<Transaction> page = new Page<>(pageNo, pageSize);
        return this.page(page, wrapper).getRecords();
    }

    @Override
    public Map<String, BigDecimal> getStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, BigDecimal> result = baseMapper.getMonthStatistics(userId, startDate, endDate);
        if (result == null) {
            result = new HashMap<>();
            result.put("totalIncome", BigDecimal.ZERO);
            result.put("totalExpense", BigDecimal.ZERO);
        }
        BigDecimal totalIncome = result.getOrDefault("totalIncome", BigDecimal.ZERO);
        BigDecimal totalExpense = result.getOrDefault("totalExpense", BigDecimal.ZERO);
        result.put("balance", totalIncome.subtract(totalExpense));
        return result;
    }

    @Override
    public List<Map<String, Object>> getMonthlyTrend(Long userId, LocalDateTime startDate) {
        return baseMapper.getMonthlyTrend(userId, startDate);
    }

    @Override
    public List<Map<String, Object>> getExpenseCategory(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return baseMapper.getExpenseCategoryStats(userId, startDate, endDate);
    }
    @Override
    public Map<String, Object> getListWithNames(Long userId, String type, Long categoryId, Long accountId,
                                                LocalDateTime startDate, LocalDateTime endDate, String keyword,
                                                int pageNo, int pageSize) {
        // 将空字符串转为 null
        if (type != null && type.trim().isEmpty()) type = null;
        if (keyword != null && keyword.trim().isEmpty()) keyword = null;

        // 打印参数（便于排查）
        System.out.println("查询参数: userId=" + userId + ", type=" + type + ", categoryId=" + categoryId 
                + ", accountId=" + accountId + ", startDate=" + startDate + ", endDate=" + endDate 
                + ", keyword=" + keyword + ", pageNo=" + pageNo + ", pageSize=" + pageSize);

        int offset = (pageNo - 1) * pageSize;
        List<TransactionVO> records = baseMapper.getListWithNames(userId, type, categoryId, accountId,
                startDate, endDate, keyword, offset, pageSize);
        long total = baseMapper.countByCondition(userId, type, categoryId, accountId,
                startDate, endDate, keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        return result;
    }
    @Override
    @Transactional
    public boolean addTransaction(Transaction transaction) {
        boolean saved = this.save(transaction);
        if (!saved) return false;

        Account account = accountMapper.selectById(transaction.getAccountId());
        if (account == null) return false;

        BigDecimal amount = transaction.getAmount();
        String type = transaction.getType();

        if ("expense".equals(type)) {
            account.setBalance(account.getBalance().subtract(amount));
            accountMapper.updateById(account);
        } else if ("income".equals(type)) {
            account.setBalance(account.getBalance().add(amount));
            accountMapper.updateById(account);
        } else if ("transfer".equals(type)) {
            account.setBalance(account.getBalance().subtract(amount));
            accountMapper.updateById(account);
            if (transaction.getToAccountId() != null) {
                Account toAccount = accountMapper.selectById(transaction.getToAccountId());
                if (toAccount != null) {
                    toAccount.setBalance(toAccount.getBalance().add(amount));
                    accountMapper.updateById(toAccount);
                }
            }
        }
        return true;
    }
}