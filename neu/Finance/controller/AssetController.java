package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.Asset;
import com.neu.Finance.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
public class AssetController {

    @Autowired
    private AssetService assetService;

    // 获取资产列表
    @GetMapping
    public Result<List<Asset>> getList(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        List<Asset> list = assetService.getList(userId);
        return Result.success(list);
    }

    // 添加资产（直接添加，不更新账户余额，建议使用 buy 接口）
    @PostMapping
    public Result<String> add(@RequestBody Asset asset, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        asset.setUserId(userId);
        boolean ok = assetService.addAsset(asset);
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    // 修改资产
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Asset asset, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        asset.setId(id);
        asset.setUserId(userId);
        boolean ok = assetService.updateAsset(asset);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    // 删除资产
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        boolean ok = assetService.deleteAsset(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }

    // 更新现价
    @PutMapping("/{id}/price")
    public Result<String> updatePrice(@PathVariable Long id, @RequestBody Map<String, BigDecimal> params, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        BigDecimal currentPrice = params.get("currentPrice");
        if (currentPrice == null) return Result.error("请提供当前价格");
        boolean ok = assetService.updatePrice(id, currentPrice);
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    // 资产汇总
    @GetMapping("/summary")
    public Result<Map<String, Object>> getSummary(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");
        Map<String, Object> summary = assetService.getSummary(userId);
        return Result.success(summary);
    }

    // ------------------ 新增买入/卖出接口 ------------------
    /**
     * 买入资产
     * 请求体：{ "name": "腾讯", "type": "stock", "accountId": 1, "quantity": 100, "costPrice": 320.5 }
     */
    @PostMapping("/buy")
    public Result<String> buyAsset(@RequestBody BuyAssetRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");

        Asset asset = new Asset();
        asset.setUserId(userId);
        asset.setName(request.getName());
        asset.setType(request.getType());
        asset.setAccountId(request.getAccountId());
        asset.setQuantity(request.getQuantity());
        asset.setCostPrice(request.getCostPrice());

        BigDecimal totalCost = request.getQuantity().multiply(request.getCostPrice());
        boolean ok = assetService.buyAsset(asset, totalCost);
        return ok ? Result.success("买入成功") : Result.error("买入失败，可能余额不足");
    }

    /**
     * 卖出资产
     * 请求体：{ "assetId": 1, "sellQuantity": 50, "sellPrice": 350 }
     */
    @PostMapping("/sell")
    public Result<String> sellAsset(@RequestBody SellAssetRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return Result.error("未登录");

        boolean ok = assetService.sellAsset(request.getAssetId(), request.getSellQuantity(), request.getSellPrice(), userId);
        return ok ? Result.success("卖出成功") : Result.error("卖出失败");
    }

    // 内部请求类
    static class BuyAssetRequest {
        private String name;
        private String type;
        private Long accountId;
        private BigDecimal quantity;
        private BigDecimal costPrice;
        // getters/setters
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
    }

    static class SellAssetRequest {
        private Long assetId;
        private BigDecimal sellQuantity;
        private BigDecimal sellPrice;
        // getters/setters
        public Long getAssetId() { return assetId; }
        public void setAssetId(Long assetId) { this.assetId = assetId; }
        public BigDecimal getSellQuantity() { return sellQuantity; }
        public void setSellQuantity(BigDecimal sellQuantity) { this.sellQuantity = sellQuantity; }
        public BigDecimal getSellPrice() { return sellPrice; }
        public void setSellPrice(BigDecimal sellPrice) { this.sellPrice = sellPrice; }
    }
}