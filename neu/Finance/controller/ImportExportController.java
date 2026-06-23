package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.service.ImportService;
import com.neu.Finance.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ImportExportController {

    @Autowired
    private ImportService importService;
    @Autowired
    private ExportService exportService;

    @PostMapping("/import/bills")
    public Result<Map<String, Object>> importBills(@RequestParam("file") MultipartFile file, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> result = importService.importBills(file, userId);
        return Result.success(result);
    }

    @GetMapping("/export/bills")
    public void exportBills(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "csv") String format,
            HttpSession session,
            HttpServletResponse response) {
        Long userId = (Long) session.getAttribute("userId");
        exportService.exportBills(userId, startDate, endDate, type, format, response);
    }
}