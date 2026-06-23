package com.neu.Finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("transaction")
public class Transaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;          // expense / income / transfer
    private BigDecimal amount;
    private Long categoryId;
    private Long accountId;
    private Long toAccountId;     // 转账目标账户
    private LocalDate date;       // 改为 LocalDate，对应前端 yyyy-MM-dd
    private String description;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableLogic
    private Integer deleted;

    // 无参构造
    public Transaction() {}

    // 全参构造
    public Transaction(Long id, Long userId, String type, BigDecimal amount, Long categoryId,
                       Long accountId, Long toAccountId, LocalDate date, String description,
                       String remark, LocalDateTime createTime, Integer deleted) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.categoryId = categoryId;
        this.accountId = accountId;
        this.toAccountId = toAccountId;
        this.date = date;
        this.description = description;
        this.remark = remark;
        this.createTime = createTime;
        this.deleted = deleted;
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public Long getToAccountId() { return toAccountId; }
    public void setToAccountId(Long toAccountId) { this.toAccountId = toAccountId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", description='" + description + '\'' +
                '}';
    }
}