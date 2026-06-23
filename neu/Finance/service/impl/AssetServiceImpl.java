package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.entity.Account;
import com.neu.Finance.entity.Asset;
import com.neu.Finance.entity.Category;
import com.neu.Finance.entity.Transaction;
import com.neu.Finance.mapper.AccountMapper;
import com.neu.Finance.mapper.AssetMapper;
import com.neu.Finance.mapper.CategoryMapper;
import com.neu.Finance.mapper.TransactionMapper;
import com.neu.Finance.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AssetServiceImpl extends ServiceImpl<AssetMapper, Asset> implements AssetService {

    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private TransactionMapper transactionMapper;
    @Autowired
    private CategoryMapper categoryMapper;

    // ================== 原有基础方法 ==================
    @Override
    public List<Asset> getList(Long userId) {
        LambdaQueryWrapper<Asset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Asset::getUserId, userId);
        wrapper.orderByDesc(Asset::getCreateTime);
        return this.list(wrapper);
    }
    
    @Override
    public BigDecimal getTotalMarketValue(Long userId) {
        List<Asset> assets = getList(userId);
        return assets.stream()
                .map(Asset::getMarketValue)   // 您的实体类有 getMarketValue() 动态计算方法
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    @Override
    public boolean addAsset(Asset asset) {
        return this.save(asset);
    }

    @Override
    public boolean updateAsset(Asset asset) {
        return this.updateById(asset);
    }

    @Override
    public boolean deleteAsset(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean updatePrice(Long id, BigDecimal currentPrice) {
        Asset asset = this.getById(id);
        if (asset == null) return false;
        asset.setCurrentPrice(currentPrice);
        return this.updateById(asset);
    }

    @Override
    public Map<String, Object> getSummary(Long userId) {
        List<Asset> list = this.getList(userId);
        BigDecimal totalMarketValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        for (Asset asset : list) {
            totalMarketValue = totalMarketValue.add(asset.getMarketValue());
            totalCost = totalCost.add(asset.getCost());
        }
        BigDecimal totalProfit = totalMarketValue.subtract(totalCost);
        BigDecimal totalProfitRate = BigDecimal.ZERO;
        if (totalCost.compareTo(BigDecimal.ZERO) != 0) {
            totalProfitRate = totalProfit.divide(totalCost, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalMarketValue", totalMarketValue);
        result.put("totalCost", totalCost);
        result.put("totalProfit", totalProfit);
        result.put("totalProfitRate", totalProfitRate);
        return result;
    }

    // ================== 新增：买入/卖出业务 ==================
    @Override
    @Transactional
    public boolean buyAsset(Asset asset, BigDecimal totalCost) {
        // 1. 扣减账户余额
        Long accountId = asset.getAccountId();
        Account account = accountMapper.selectById(accountId);
        if (account == null) return false;
        BigDecimal newBalance = account.getBalance().subtract(totalCost);
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) return false; // 余额不足
        account.setBalance(newBalance);
        accountMapper.updateById(account);

        // 2. 设置资产当前价 = 成本价
        asset.setCurrentPrice(asset.getCostPrice());
        this.save(asset);

        // 3. 生成支出交易记录（分类“投资”）
        Long investCategoryId = getOrCreateCategory(asset.getUserId(), "投资", "expense");
        Transaction transaction = new Transaction();
        transaction.setUserId(asset.getUserId());
        transaction.setType("expense");
        transaction.setAmount(totalCost);
        transaction.setCategoryId(investCategoryId);
        transaction.setAccountId(accountId);
        transaction.setDate(LocalDate.now());
        transaction.setDescription("买入" + asset.getName());
        transactionMapper.insert(transaction);

        return true;
    }

    @Override
    @Transactional
    public boolean sellAsset(Long assetId, BigDecimal sellQuantity, BigDecimal sellPrice, Long userId) {
        Asset asset = this.getById(assetId);
        if (asset == null || !asset.getUserId().equals(userId)) return false;
        if (sellQuantity.compareTo(asset.getQuantity()) > 0) return false;

        BigDecimal sellTotal = sellQuantity.multiply(sellPrice);

        // 1. 增加账户余额
        Account account = accountMapper.selectById(asset.getAccountId());
        if (account == null) return false;
        account.setBalance(account.getBalance().add(sellTotal));
        accountMapper.updateById(account);

        // 2. 处理资产记录
        if (sellQuantity.compareTo(asset.getQuantity()) == 0) {
            // 全部卖出，删除资产
            this.removeById(assetId);
        } else {
            // 部分卖出：减少数量，重新计算成本价（移动平均）
            BigDecimal remainingQuantity = asset.getQuantity().subtract(sellQuantity);
            BigDecimal originalCostTotal = asset.getQuantity().multiply(asset.getCostPrice());
            BigDecimal soldCost = sellQuantity.multiply(asset.getCostPrice());
            BigDecimal newCostPrice = originalCostTotal.subtract(soldCost).divide(remainingQuantity, 4, RoundingMode.HALF_UP);
            asset.setQuantity(remainingQuantity);
            asset.setCostPrice(newCostPrice);
            this.updateById(asset);
        }

        // 3. 生成收入交易记录（分类“投资收益”）
        Long investIncomeCategoryId = getOrCreateCategory(userId, "投资收益", "income");
        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setType("income");
        transaction.setAmount(sellTotal);
        transaction.setCategoryId(investIncomeCategoryId);
        transaction.setAccountId(asset.getAccountId());
        transaction.setDate(LocalDate.now());
        transaction.setDescription("卖出" + asset.getName());
        transactionMapper.insert(transaction);

        return true;
    }

    // ================== 辅助方法 ==================
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
            category.setIcon("TrendCharts");
            category.setColor("#409eff");
            categoryMapper.insert(category);
        }
        return category.getId();
    }
}