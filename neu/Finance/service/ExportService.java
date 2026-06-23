package com.neu.Finance.service;

import jakarta.servlet.http.HttpServletResponse;

public interface ExportService {
    void exportBills(Long userId, String startDate, String endDate, String type, String format, HttpServletResponse response);
}