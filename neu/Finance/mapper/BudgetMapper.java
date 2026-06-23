package com.neu.Finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.Finance.entity.Budget;
import com.neu.Finance.dto.BudgetSummaryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BudgetMapper extends BaseMapper<Budget> {

    /**
     * 查询某用户某月的预算汇总（包含实际支出）
     */
	@Select("SELECT " +
	        "    b.category_id as categoryId, " +
	        "    c.name as categoryName, " +
	        "    b.budget_amount as budgetAmount, " +
	        "    COALESCE(SUM(t.amount), 0) as actualAmount " +
	        "FROM budget_plan b " +   // 关键修改
	        "LEFT JOIN category c ON b.category_id = c.id " +
	        "LEFT JOIN transaction t ON t.category_id = b.category_id " +
	        "    AND t.user_id = b.user_id " +
	        "    AND t.type = 'expense' " +
	        "    AND DATE_FORMAT(t.date, '%Y-%m') = #{yearMonth} " +
	        "    AND t.deleted = 0 " +
	        "WHERE b.user_id = #{userId} " +
	        "    AND b.year_month = #{yearMonth} " +
	        "    AND b.deleted = 0 " +
	        "GROUP BY b.category_id, c.name, b.budget_amount")
	List<BudgetSummaryDTO> getBudgetSummary(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);}