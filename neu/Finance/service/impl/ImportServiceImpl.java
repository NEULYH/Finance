package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.neu.Finance.dto.ImportRecordDTO;
import com.neu.Finance.entity.*;
import com.neu.Finance.mapper.*;
import com.neu.Finance.service.ImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class ImportServiceImpl implements ImportService {

    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private AccountMapper accountMapper;

    /**
     * 解析多种常见日期时间格式
     * 支持：
     *   yyyy-MM-dd HH:mm:ss
     *   yyyy-MM-dd HH:mm
     *   yyyy/M/d H:mm
     *   yyyy/M/d HH:mm
     *   yyyy/MM/dd HH:mm:ss
     *   yyyy/MM/dd HH:mm
     *   yyyy-M-d H:mm
     *   yyyy-M-d HH:mm
     */
    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new RuntimeException("日期为空");
        }
        String trimmed = dateStr.trim();
        // 常见格式列表
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy/M/d H:mm",
            "yyyy/M/d HH:mm",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy-M-d H:mm",
            "yyyy-M-d HH:mm"
        };
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDateTime.parse(trimmed, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        // 如果以上都不匹配，尝试只解析日期部分，时间默认为 00:00:00
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            java.time.LocalDate date = java.time.LocalDate.parse(trimmed, dateFormatter);
            return date.atStartOfDay();
        } catch (DateTimeParseException ignored) {
        }
        throw new RuntimeException("无法解析日期: " + dateStr);
    }

    private String inferCategory(String description, String type) {
        if (description == null) return "其他";
        description = description.toLowerCase();
        if ("expense".equals(type)) {
            if (description.contains("餐") || description.contains("饭") || description.contains("麦当劳")) return "餐饮";
            if (description.contains("超市") || description.contains("购物")) return "购物";
            if (description.contains("车") || description.contains("公交") || description.contains("地铁")) return "交通";
            if (description.contains("电影") || description.contains("游戏")) return "娱乐";
            return "其他";
        } else {
            if (description.contains("工资") || description.contains("薪水")) return "工资";
            if (description.contains("报销")) return "报销";
            return "其他收入";
        }
    }

    @Override
    public Map<String, Object> importBills(MultipartFile file, Long userId) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), "UTF-8"))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length < 6) {
                    failCount++;
                    errors.add("无效行（字段数不足）: " + line);
                    continue;
                }
                try {
                    // 1. 解析日期
                    LocalDateTime dateTime = parseDateTime(parts[0]);
                    java.time.LocalDate date = dateTime.toLocalDate();

                    String type = parts[1].trim();
                    String categoryName = parts[2].trim().isEmpty() ? null : parts[2].trim();
                    String accountName = parts[3].trim().isEmpty() ? null : parts[3].trim();
                    BigDecimal amount = new BigDecimal(parts[4].trim());
                    String description = parts[5].trim();
                    String remark = parts.length > 6 ? parts[6].trim() : "";

                    // 2. 查找或创建账户
                    String accName = accountName == null ? "现金" : accountName;
                    Account account = accountMapper.selectOne(
                            new LambdaQueryWrapper<Account>()
                                    .eq(Account::getUserId, userId)
                                    .eq(Account::getName, accName)
                    );
                    if (account == null) {
                        account = new Account();
                        account.setUserId(userId);
                        account.setName(accName);
                        account.setType("cash");
                        account.setCurrency("CNY");
                        account.setBalance(BigDecimal.ZERO);
                        accountMapper.insert(account);
                    }

                    // 3. 确定分类
                    String catName = categoryName;
                    if (catName == null || catName.isEmpty()) {
                        catName = inferCategory(description, type);
                    }
                    Category category = categoryMapper.selectOne(
                            new LambdaQueryWrapper<Category>()
                                    .eq(Category::getUserId, userId)
                                    .eq(Category::getName, catName)
                                    .eq(Category::getType, type)
                    );
                    if (category == null) {
                        category = new Category();
                        category.setUserId(userId);
                        category.setName(catName);
                        category.setType(type);
                        categoryMapper.insert(category);
                    }

                    // 4. 创建交易记录（假设实体中 date 为 LocalDate）
                    Transaction transaction = new Transaction();
                    transaction.setUserId(userId);
                    transaction.setType(type);
                    transaction.setAmount(amount);
                    transaction.setCategoryId(category.getId());
                    transaction.setAccountId(account.getId());
                    transaction.setDate(date);          // 如果实体是 LocalDate
                    transaction.setDescription(description);
                    transaction.setRemark(remark);
                    transactionMapper.insert(transaction);

                    // 5. 更新账户余额
                    if ("expense".equals(type)) {
                        account.setBalance(account.getBalance().subtract(amount));
                    } else {
                        account.setBalance(account.getBalance().add(amount));
                    }
                    accountMapper.updateById(account);

                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("解析行失败: " + line + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("文件解析失败", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", successCount);
        result.put("fail", failCount);
        result.put("errors", errors);
        return result;
    }
}