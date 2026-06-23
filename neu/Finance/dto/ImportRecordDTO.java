package com.neu.Finance.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ImportRecordDTO {
    private LocalDateTime date;
    private String description;
    private BigDecimal amount;
    private String type;      // expense / income
    private String categoryName; // 原始分类名称（用于匹配）
    private String accountName;
    private String remark;

    // 无参构造
    public ImportRecordDTO() {}

    // Getters and Setters
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}