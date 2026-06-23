package com.neu.Finance.service.impl;

import com.neu.Finance.dto.ExportBillDTO;
import com.neu.Finance.mapper.TransactionMapper;
import com.neu.Finance.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ExportServiceImpl implements ExportService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Override
    public void exportBills(Long userId, String startDateStr, String endDateStr, String type, String format, HttpServletResponse response) {
        // 只支持 CSV 格式
        try {
            LocalDateTime startDate = startDateStr != null ? LocalDate.parse(startDateStr).atStartOfDay() : null;
            LocalDateTime endDate = endDateStr != null ? LocalDate.parse(endDateStr).atTime(23, 59, 59) : null;

            // 查询数据（联查获取分类名和账户名）
            List<Map<String, Object>> bills = transactionMapper.exportList(userId, startDate, endDate, type);

            // 设置响应头
            response.setContentType("text/csv;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=bills.csv");
            PrintWriter writer = response.getWriter();

            // 写入 UTF-8 BOM（解决中文乱码）
            writer.write('\uFEFF');

            // 写入表头
            writer.println("日期,类型,分类,账户,描述,金额,备注");

            for (Map<String, Object> row : bills) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCsv(row.get("date") != null ? row.get("date").toString() : "")).append(",");
                String typeValue = row.get("type").toString();
                sb.append(escapeCsv("expense".equals(typeValue) ? "支出" : "收入")).append(",");
                sb.append(escapeCsv(row.get("categoryName") != null ? row.get("categoryName").toString() : "")).append(",");
                sb.append(escapeCsv(row.get("accountName") != null ? row.get("accountName").toString() : "")).append(",");
                sb.append(escapeCsv(row.get("description") != null ? row.get("description").toString() : "")).append(",");
                sb.append(row.get("amount")).append(",");
                sb.append(escapeCsv(row.get("remark") != null ? row.get("remark").toString() : ""));
                writer.println(sb.toString());
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("导出失败", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // 如果包含逗号、引号或换行符，则用双引号包裹并转义内部引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}