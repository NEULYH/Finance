package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.dto.MonthlyReportDTO;
import com.neu.Finance.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/monthly")
    public Result<MonthlyReportDTO> getMonthlyReport(@RequestParam String yearMonth, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        MonthlyReportDTO report = reportService.generateMonthlyReport(userId, yearMonth);
        return Result.success(report);
    }
}