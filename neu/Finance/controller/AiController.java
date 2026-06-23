package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.service.AiReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiReportService aiReportService;

    @PostMapping("/parse-transaction")
    public Result<Map<String, Object>> parseTransaction(@RequestBody Map<String, String> request) {
        System.out.println("====== 收到 /api/ai/parse-transaction 请求 ======");
        String text = request.get("text");
        if (text == null || text.trim().isEmpty()) {
            return Result.error("请输入记账文本");
        }
        try {
            Map<String, Object> result = aiReportService.parseTransactionText(text);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("解析失败：" + e.getMessage());
        }
    }
}