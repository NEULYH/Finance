package com.neu.Finance.service;

import com.neu.Finance.dto.MonthlyReportDTO;
import java.util.Map;

public interface AiReportService {
    MonthlyReportDTO enrichWithAi(MonthlyReportDTO report);
    Map<String, Object> parseTransactionText(String text);   // 新增
}