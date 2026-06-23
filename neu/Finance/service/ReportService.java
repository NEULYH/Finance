package com.neu.Finance.service;

import com.neu.Finance.dto.MonthlyReportDTO;

public interface ReportService {
    MonthlyReportDTO generateMonthlyReport(Long userId, String yearMonth);
}