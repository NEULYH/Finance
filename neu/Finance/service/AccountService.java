package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.Account;
import java.math.BigDecimal;
import java.util.List;

public interface AccountService extends IService<Account> {
    List<Account> getUserAccounts(Long userId);
    boolean addAccount(Account account);
    boolean updateAccount(Account account);
    boolean deleteAccount(Long id, Long userId);
    boolean updateBalance(Long accountId, BigDecimal change, Long userId);
    BigDecimal getTotalAsset(Long userId);
}