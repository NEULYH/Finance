package com.neu.Finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.Finance.entity.Account;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
	@Select("SELECT COALESCE(SUM(balance), 0) FROM account WHERE user_id = #{userId} AND deleted = 0")
	BigDecimal getTotalBalance(Long userId);
}