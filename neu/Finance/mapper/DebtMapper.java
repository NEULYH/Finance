package com.neu.Finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.Finance.entity.Debt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Map;

@Mapper
public interface DebtMapper extends BaseMapper<Debt> {

    @Select("SELECT COALESCE(SUM(CASE WHEN type = 'lend' THEN remaining_amount ELSE 0 END), 0) as totalLend, " +
            "COALESCE(SUM(CASE WHEN type = 'borrow' THEN remaining_amount ELSE 0 END), 0) as totalBorrow " +
            "FROM debt WHERE user_id = #{userId} AND deleted = 0")
    Map<String, BigDecimal> getDebtSummary(Long userId);
}