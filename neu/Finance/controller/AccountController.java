package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.Account;
import com.neu.Finance.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping
    public Result<List<Account>> getAccounts(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<Account> accounts = accountService.getUserAccounts(userId);
        return Result.success(accounts);
    }

    @PostMapping
    public Result<String> addAccount(@RequestBody Account account, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        account.setUserId(userId);
        boolean ok = accountService.addAccount(account);
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> updateAccount(@PathVariable Long id, @RequestBody Account account, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        account.setId(id);
        account.setUserId(userId);
        boolean ok = accountService.updateAccount(account);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteAccount(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean ok = accountService.deleteAccount(id, userId);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/total-asset")
    public Result<BigDecimal> getTotalAsset(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        BigDecimal totalAsset = accountService.getTotalAsset(userId);
        return Result.success(totalAsset);
    }
}