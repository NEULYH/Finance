package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.Debt;
import com.neu.Finance.service.DebtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debts")
public class DebtController {

    @Autowired
    private DebtService debtService;

    @GetMapping
    public Result<List<Debt>> list(@RequestParam(required = false) String type, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<Debt> list = debtService.getUserDebts(userId, type);
        return Result.success(list);
    }

    // 新增债务（需要传入 accountId）
    @PostMapping
    public Result<String> add(@RequestBody AddDebtRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        if (request.getAccountId() == null) return Result.error("请选择关联账户");

        Debt debt = new Debt();
        debt.setUserId(userId);
        debt.setType(request.getType());
        debt.setContact(request.getContact());
        debt.setAmount(request.getAmount());
        debt.setInterestRate(request.getInterestRate());
        debt.setDueDate(request.getDueDate());
        debt.setRemark(request.getRemark());
        debt.setRemainingAmount(request.getAmount());

        boolean ok = debtService.addDebt(debt, request.getAccountId());
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Debt debt, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        debt.setId(id);
        debt.setUserId(userId);
        boolean ok = debtService.updateDebt(debt);
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean ok = debtService.deleteDebt(id, userId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 还款/收款（需要传入 accountId 和 amount）
    @PostMapping("/{id}/repay")
    public Result<String> repay(@PathVariable Long id, @RequestBody RepayRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("还款金额必须大于0");
        }
        if (request.getAccountId() == null) return Result.error("请选择还款/收款账户");
        boolean ok = debtService.repayDebt(id, amount, userId, request.getAccountId());
        return ok ? Result.success("还款成功") : Result.error("还款失败，请检查金额");
    }

    @GetMapping("/summary")
    public Result<Map<String, BigDecimal>> summary(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Map<String, BigDecimal> summary = debtService.getDebtSummary(userId);
        return Result.success(summary);
    }

    // 内部请求类
    static class AddDebtRequest {
        private String type;        // borrow / lend
        private String contact;
        private BigDecimal amount;
        private BigDecimal interestRate;
        private LocalDate dueDate;
        private String remark;
        private Long accountId;

        // getters/setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getInterestRate() { return interestRate; }
        public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
    }

    static class RepayRequest {
        private BigDecimal amount;
        private Long accountId;

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
    }
}