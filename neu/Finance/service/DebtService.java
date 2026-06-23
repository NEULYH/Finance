package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.Debt;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DebtService extends IService<Debt> {
    List<Debt> getUserDebts(Long userId, String type);
    boolean addDebt(Debt debt, Long accountId);               // 新增 accountId 参数
    boolean updateDebt(Debt debt);
    boolean deleteDebt(Long id, Long userId);
    boolean repayDebt(Long id, BigDecimal amount, Long userId, Long accountId); // 新增 accountId
    Map<String, BigDecimal> getDebtSummary(Long userId);
}