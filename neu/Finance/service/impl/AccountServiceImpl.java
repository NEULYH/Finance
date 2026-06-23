package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.entity.Account;
import com.neu.Finance.mapper.AccountMapper;
import com.neu.Finance.service.AccountService;
import com.neu.Finance.service.AssetService;
import com.neu.Finance.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountService {

    @Autowired
    private AssetService assetService;
    @Autowired
    private DebtService debtService;

    @Override
    public List<Account> getUserAccounts(Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId)
               .orderByDesc(Account::getCreateTime);
        return this.list(wrapper);
    }

    @Override
    public boolean addAccount(Account account) {
        if (account.getBalance() == null) account.setBalance(BigDecimal.ZERO);
        return this.save(account);
    }

    @Override
    public boolean updateAccount(Account account) {
        return this.updateById(account);
    }

    @Override
    @Transactional
    public boolean deleteAccount(Long id, Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getId, id).eq(Account::getUserId, userId);
        Account account = this.getOne(wrapper);
        if (account == null) return false;
        return this.removeById(id);
    }

    @Override
    @Transactional
    public boolean updateBalance(Long accountId, BigDecimal change, Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getId, accountId).eq(Account::getUserId, userId);
        Account account = this.getOne(wrapper);
        if (account == null) return false;
        BigDecimal newBalance = account.getBalance().add(change);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) return false;
        account.setBalance(newBalance);
        return this.updateById(account);
    }

    /**
     * 计算用户的净资产 = 所有账户余额之和 + 资产市值（可选） + 应收债权 - 应付债务
     */
    @Override
    public BigDecimal getTotalAsset(Long userId) {
        // 1. 账户余额总和
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId);
        BigDecimal totalBalance = this.list(wrapper).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. 资产市值总和（如不需要，可删除以下三行）
        // BigDecimal totalMarketValue = assetService.getTotalMarketValue(userId);
        // 如果不需要资产市值，直接注释或设置总市值为0
        BigDecimal totalMarketValue = BigDecimal.ZERO;  // 默认不计算资产，如有需要可取消注释

        // 3. 债务汇总（借出未收 - 借入未还）
        Map<String, BigDecimal> debtSummary = debtService.getDebtSummary(userId);
        BigDecimal totalLend = debtSummary.getOrDefault("totalLend", BigDecimal.ZERO);    // 借出未收
        BigDecimal totalBorrow = debtSummary.getOrDefault("totalBorrow", BigDecimal.ZERO); // 借入未还
        BigDecimal netDebt = totalLend.subtract(totalBorrow); // 净债权（正数表示应收多于应付）

        // 4. 净资产 = 账户余额 + 资产市值 + 净债权
        return totalBalance.add(totalMarketValue).add(netDebt);
    }

    // 如果需要单独查询账户余额总和（不包含债务和资产），可以添加以下方法
    public BigDecimal getTotalBalanceOnly(Long userId) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId);
        return this.list(wrapper).stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}