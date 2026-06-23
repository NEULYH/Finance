package com.neu.Finance.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("asset")
public class Asset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String type;       // stock, fund, crypto
    private Long accountId;
    private BigDecimal quantity;
    private BigDecimal costPrice;
    private BigDecimal currentPrice;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;

    // 无参构造
    public Asset() {}

    // 全参构造（可选）
    public Asset(Long id, Long userId, String name, String type, Long accountId,
                 BigDecimal quantity, BigDecimal costPrice, BigDecimal currentPrice,
                 String remark, LocalDateTime createTime, LocalDateTime updateTime, Integer deleted) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.accountId = accountId;
        this.quantity = quantity;
        this.costPrice = costPrice;
        this.currentPrice = currentPrice;
        this.remark = remark;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.deleted = deleted;
    }

    // Getter / Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }

    // 计算属性（非持久化）
    public BigDecimal getMarketValue() {
        if (quantity == null || currentPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(currentPrice);
    }

    public BigDecimal getCost() {
        if (quantity == null || costPrice == null) return BigDecimal.ZERO;
        return quantity.multiply(costPrice);
    }

    public BigDecimal getProfit() {
        return getMarketValue().subtract(getCost());
    }

    public BigDecimal getProfitRate() {
        BigDecimal cost = getCost();
        if (cost.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return getProfit().divide(cost, 4, BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}