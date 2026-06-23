package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.entity.*;
import com.neu.Finance.mapper.*;
import com.neu.Finance.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DebtServiceImpl extends ServiceImpl<DebtMapper, Debt> implements DebtService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Debt> getUserDebts(Long userId, String type) {
        LambdaQueryWrapper<Debt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Debt::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Debt::getType, type);
        }
        wrapper.orderByDesc(Debt::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    @Transactional
    public boolean addDebt(Debt debt, Long accountId) {
        // 1. 参数校验
        if (accountId == null) return false;
        debt.setAccountId(accountId);
        if (debt.getRemainingAmount() == null) {
            debt.setRemainingAmount(debt.getAmount());
        }

        // 2. 更新账户余额
        Account account = accountMapper.selectById(accountId);
        if (account == null) return false;

        BigDecimal change;
        String transactionType;
        String categoryName;
        if ("borrow".equals(debt.getType())) {
            // 借入：账户余额增加
            change = debt.getAmount();
            transactionType = "income";
            categoryName = "债务借入";
        } else if ("lend".equals(debt.getType())) {
            // 借出：账户余额减少
            change = debt.getAmount().negate();
            transactionType = "expense";
            categoryName = "债务借出";
        } else {
            return false;
        }

        BigDecimal newBalance = account.getBalance().add(change);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) return false;
        account.setBalance(newBalance);
        accountMapper.updateById(account);

        // 3. 保存债务记录
        boolean saved = this.save(debt);
        if (!saved) return false;

        // 4. 生成交易记录
        Long categoryId = getOrCreateCategory(debt.getUserId(), categoryName, transactionType);
        Transaction transaction = new Transaction();
        transaction.setUserId(debt.getUserId());
        transaction.setType(transactionType);
        transaction.setAmount(debt.getAmount());
        transaction.setCategoryId(categoryId);
        transaction.setAccountId(accountId);
        transaction.setDate(LocalDate.now());
        transaction.setDescription(("borrow".equals(debt.getType()) ? "借入" : "借出") + "：" + debt.getContact());
        transactionMapper.insert(transaction);

        return true;
    }

    @Override
    public boolean updateDebt(Debt debt) {
        // 仅更新债务信息，不影响账户余额
        return this.updateById(debt);
    }

    @Override
    public boolean deleteDebt(Long id, Long userId) {
        LambdaQueryWrapper<Debt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Debt::getId, id).eq(Debt::getUserId, userId);
        return this.remove(wrapper);
    }

    @Override
    @Transactional
    public boolean repayDebt(Long id, BigDecimal amount, Long userId, Long accountId) {
        // 1. 获取债务记录
        Debt debt = this.getById(id);
        if (debt == null || !debt.getUserId().equals(userId)) return false;
        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(debt.getRemainingAmount()) > 0) {
            return false;
        }

        // 2. 更新账户余额
        Account account = accountMapper.selectById(accountId);
        if (account == null) return false;

        BigDecimal change;
        String transactionType;
        String categoryName;
        if ("borrow".equals(debt.getType())) {
            // 归还借款：账户余额减少
            change = amount.negate();
            transactionType = "expense";
            categoryName = "还款";
        } else if ("lend".equals(debt.getType())) {
            // 收回借出款：账户余额增加
            change = amount;
            transactionType = "income";
            categoryName = "收回借款";
        } else {
            return false;
        }

        BigDecimal newBalance = account.getBalance().add(change);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) return false;
        account.setBalance(newBalance);
        accountMapper.updateById(account);

        // 3. 更新债务剩余金额
        debt.setRemainingAmount(debt.getRemainingAmount().subtract(amount));
        boolean updated = this.updateById(debt);
        if (!updated) return false;

        // 4. 生成交易记录
        Long categoryId = getOrCreateCategory(debt.getUserId(), categoryName, transactionType);
        Transaction transaction = new Transaction();
        transaction.setUserId(debt.getUserId());
        transaction.setType(transactionType);
        transaction.setAmount(amount);
        transaction.setCategoryId(categoryId);
        transaction.setAccountId(accountId);
        transaction.setDate(LocalDate.now());
        transaction.setDescription(("borrow".equals(debt.getType()) ? "还款给" : "收回借款从") + debt.getContact());
        transactionMapper.insert(transaction);

        return true;
    }

    @Override
    public Map<String, BigDecimal> getDebtSummary(Long userId) {
        // 直接使用 Mapper 中的 SQL 查询（已在 DebtMapper 中定义）
        return baseMapper.getDebtSummary(userId);
    }

    // ========== 辅助方法 ==========
    private Long getOrCreateCategory(Long userId, String name, String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
               .eq(Category::getName, name)
               .eq(Category::getType, type);
        Category category = categoryMapper.selectOne(wrapper);
        if (category == null) {
            category = new Category();
            category.setUserId(userId);
            category.setName(name);
            category.setType(type);
            category.setIcon("CreditCard");
            category.setColor("#f39c12");
            categoryMapper.insert(category);
        }
        return category.getId();
    }
}