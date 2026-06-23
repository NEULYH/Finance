package com.neu.Finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.neu.Finance.dto.TransactionVO;
import com.neu.Finance.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface TransactionMapper extends BaseMapper<Transaction> {

    @Select("SELECT COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) as totalIncome, " +
            "COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0) as totalExpense " +
            "FROM transaction WHERE user_id = #{userId} AND deleted = 0 " +
            "AND date BETWEEN #{startDate} AND #{endDate}")
    Map<String, BigDecimal> getMonthStatistics(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Select("SELECT DATE_FORMAT(date, '%Y-%m') as month, " +
            "COALESCE(SUM(CASE WHEN type = 'income' THEN amount ELSE 0 END), 0) as income, " +
            "COALESCE(SUM(CASE WHEN type = 'expense' THEN amount ELSE 0 END), 0) as expense " +
            "FROM transaction WHERE user_id = #{userId} AND deleted = 0 " +
            "AND date >= #{startDate} GROUP BY month ORDER BY month")
    List<Map<String, Object>> getMonthlyTrend(Long userId, LocalDateTime startDate);

    @Select("SELECT c.name as categoryName, COALESCE(SUM(t.amount), 0) as amount " +
            "FROM transaction t LEFT JOIN category c ON t.category_id = c.id " +
            "WHERE t.user_id = #{userId} AND t.type = 'expense' AND t.deleted = 0 " +
            "AND t.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY t.category_id, c.name")
    List<Map<String, Object>> getExpenseCategoryStats(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Select("SELECT c.name as categoryName, COALESCE(SUM(t.amount), 0) as amount " +
            "FROM transaction t LEFT JOIN category c ON t.category_id = c.id " +
            "WHERE t.user_id = #{userId} AND t.type = 'income' AND t.deleted = 0 " +
            "AND t.date BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY t.category_id, c.name")
    List<Map<String, Object>> getIncomeCategoryStats(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    @Select("SELECT t.date, t.type, c.name as categoryName, a.name as accountName, " +
            "t.description, t.amount, t.remark " +
            "FROM transaction t " +
            "LEFT JOIN category c ON t.category_id = c.id " +
            "LEFT JOIN account a ON t.account_id = a.id " +
            "WHERE t.user_id = #{userId} AND t.deleted = 0 " +
            "AND (#{startDate} IS NULL OR t.date >= #{startDate}) " +
            "AND (#{endDate} IS NULL OR t.date <= #{endDate}) " +
            "AND (#{type} IS NULL OR t.type = #{type}) " +
            "ORDER BY t.date DESC")
    List<Map<String, Object>> exportList(Long userId, LocalDateTime startDate, LocalDateTime endDate, String type);
    @Select("SELECT t.*, c.name as categoryName, a.name as accountName " +
            "FROM transaction t " +
            "LEFT JOIN category c ON t.category_id = c.id " +
            "LEFT JOIN account a ON t.account_id = a.id " +
            "WHERE t.user_id = #{userId} AND t.deleted = 0 " +
            "AND (#{type} IS NULL OR t.type = #{type}) " +
            "AND (#{categoryId} IS NULL OR t.category_id = #{categoryId}) " +
            "AND (#{accountId} IS NULL OR t.account_id = #{accountId}) " +
            "AND (#{startDate} IS NULL OR t.date >= #{startDate}) " +
            "AND (#{endDate} IS NULL OR t.date <= #{endDate}) " +
            "AND (#{keyword} IS NULL OR t.description LIKE CONCAT('%', #{keyword}, '%') OR t.remark LIKE CONCAT('%', #{keyword}, '%')) " +
            "ORDER BY t.date DESC " +
            "LIMIT #{offset}, #{pageSize}")
    List<TransactionVO> getListWithNames(@Param("userId") Long userId,
                                         @Param("type") String type,
                                         @Param("categoryId") Long categoryId,
                                         @Param("accountId") Long accountId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate,
                                         @Param("keyword") String keyword,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM transaction t " +
            "WHERE t.user_id = #{userId} AND t.deleted = 0 " +
            "AND (#{type} IS NULL OR t.type = #{type}) " +
            "AND (#{categoryId} IS NULL OR t.category_id = #{categoryId}) " +
            "AND (#{accountId} IS NULL OR t.account_id = #{accountId}) " +
            "AND (#{startDate} IS NULL OR t.date >= #{startDate}) " +
            "AND (#{endDate} IS NULL OR t.date <= #{endDate}) " +
            "AND (#{keyword} IS NULL OR t.description LIKE CONCAT('%', #{keyword}, '%') OR t.remark LIKE CONCAT('%', #{keyword}, '%'))")
    long countByCondition(@Param("userId") Long userId,
                          @Param("type") String type,
                          @Param("categoryId") Long categoryId,
                          @Param("accountId") Long accountId,
                          @Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate,
                          @Param("keyword") String keyword);
}