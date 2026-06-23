package com.neu.Finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.Finance.entity.Asset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AssetMapper extends BaseMapper<Asset> {

    @Select("SELECT COALESCE(SUM(market_value), 0) as totalMarketValue, " +
            "COALESCE(SUM(quantity * cost_price), 0) as totalCost " +
            "FROM asset WHERE user_id = #{userId} AND deleted = 0")
    Map<String, BigDecimal> getAssetSummary(Long userId);

    @Select("SELECT type, COALESCE(SUM(market_value), 0) as totalValue " +
            "FROM asset WHERE user_id = #{userId} AND deleted = 0 " +
            "GROUP BY type")
    List<Map<String, Object>> getAssetGroupByType(Long userId);
}