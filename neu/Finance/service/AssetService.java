package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.Asset;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AssetService extends IService<Asset> {
    List<Asset> getList(Long userId);
    boolean addAsset(Asset asset);
    boolean updateAsset(Asset asset);
    boolean deleteAsset(Long id);
    boolean updatePrice(Long id, BigDecimal currentPrice);
    boolean buyAsset(Asset asset, BigDecimal totalCost);
    BigDecimal getTotalMarketValue(Long userId);
    boolean sellAsset(Long assetId, BigDecimal sellQuantity, BigDecimal sellPrice, Long userId);
    Map<String, Object> getSummary(Long userId); // 总市值、总成本、总盈亏
}