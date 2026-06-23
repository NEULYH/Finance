package com.neu.Finance.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neu.Finance.dto.MonthlyReportDTO;
import com.neu.Finance.service.AiReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class AiReportServiceImpl implements AiReportService {

    private static final Logger log = LoggerFactory.getLogger(AiReportServiceImpl.class);

    @Value("${deepseek.api.url:https://api.deepseek.com/chat/completions}")
    private String apiUrl;

    @Value("${deepseek.api.key:}")
    private String apiKey;

    @Value("${deepseek.model:deepseek-chat}")
    private String model;

    @Value("${deepseek.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiReportServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // ========== 月度报告 AI 生成 ==========
    @Override
    public MonthlyReportDTO enrichWithAi(MonthlyReportDTO report) {
        try {
            String prompt = buildPrompt(report);
            log.info("调用 AI API，prompt 长度: {}", prompt.length());
            String aiResponse = callDeepSeek(prompt);
            log.info("AI 原始响应: {}", aiResponse);
            
            // 直接输出原始响应，不做任何解析
            report.setAiInsight(aiResponse);
            report.setSuggestions(null);   // 不使用 suggestions 数组
            
        } catch (Exception e) {
            log.error("AI 调用失败，使用本地规则生成", e);
            fallbackGenerate(report);
        }
        return report;
    }
    private String buildPrompt(MonthlyReportDTO report) {
        // 支出排行
        StringBuilder expenseRankingStr = new StringBuilder();
        if (report.getExpenseRanking() != null && !report.getExpenseRanking().isEmpty()) {
            expenseRankingStr.append("支出排行：");
            for (int i = 0; i < report.getExpenseRanking().size(); i++) {
                var item = report.getExpenseRanking().get(i);
                expenseRankingStr.append(String.format("%s: %.2f元", item.get("categoryName"), item.get("amount")));
                if (i < report.getExpenseRanking().size() - 1) expenseRankingStr.append("，");
            }
        }
        // 收入排行
        StringBuilder incomeRankingStr = new StringBuilder();
        if (report.getIncomeRanking() != null && !report.getIncomeRanking().isEmpty()) {
            incomeRankingStr.append("收入排行：");
            for (int i = 0; i < report.getIncomeRanking().size(); i++) {
                var item = report.getIncomeRanking().get(i);
                incomeRankingStr.append(String.format("%s: %.2f元", item.get("categoryName"), item.get("amount")));
                if (i < report.getIncomeRanking().size() - 1) incomeRankingStr.append("，");
            }
        }

        return String.format(
            "你是一个专业的个人财务顾问。请根据以下月度财务数据，生成一份详细的财务分析报告。要求包含：\n" +
            "1. 总体财务健康评估\n2. 收入结构分析\n3. 支出分析\n4. 至少3条具体可执行的理财建议\n\n" +
            "数据：\n- 总收入：%.2f元\n- 总支出：%.2f元\n- 结余：%.2f元\n- 结余率：%.2f%%\n%s\n%s\n\n" +
            "输出格式：\n洞察：<综合财务洞察>\n建议：\n- <建议1>\n- <建议2>\n- <建议3>\n",
            report.getTotalIncome(), report.getTotalExpense(), report.getSurplus(), report.getSurplusRate(),
            expenseRankingStr.toString(), incomeRankingStr.toString()
        );
    }

    // ========== 智能记账解析 ==========
    @Override
    public Map<String, Object> parseTransactionText(String text) {
        String prompt = String.format(
            "你是一个记账助手。请解析以下记账文本，提取交易信息。返回严格的JSON格式，不要有任何额外说明。字段包括：type（expense/income）、amount（数字）、category（分类名，从[餐饮,购物,交通,娱乐,医疗,工资,奖金,其他]中选择）、account（账户名，从[支付宝,微信,现金,银行卡]中选择或根据文本推断）、date（yyyy-MM-dd，如果用户没给则使用今天日期2026-05-26）、description（简短描述）。\n文本：\"%s\"\n输出JSON：",
            text
        );
        String aiResponse = callDeepSeek(prompt);
        // 清理可能的 markdown 标记
        aiResponse = aiResponse.replaceAll("```json\\n?", "").replaceAll("\\n?```", "").trim();
        try {
            return objectMapper.readValue(aiResponse, Map.class);
        } catch (Exception e) {
            log.error("解析AI响应JSON失败: {}", aiResponse, e);
            throw new RuntimeException("解析失败", e);
        }
    }

    // ========== 通用 AI 调用 ==========
    private String callDeepSeek(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("stream", false);

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 序列化失败", e);
        }

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            try {
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            } catch (JsonProcessingException e) {
                throw new RuntimeException("响应解析失败", e);
            }
        } else {
            throw new RuntimeException("API 调用失败，状态码: " + response.getStatusCode());
        }
    }

    private void parseAiResponse(String aiResponse, MonthlyReportDTO report) {
        String[] lines = aiResponse.split("\n");
        String insight = "";
        List<String> suggestions = new ArrayList<>();
        boolean inSuggestions = false;
        for (String line : lines) {
            if (line.startsWith("洞察：") || line.startsWith("洞察:")) {
                insight = line.substring(line.indexOf("：") + 1).trim();
                if (insight.isEmpty()) insight = line.substring(line.indexOf(":") + 1).trim();
            } else if (line.startsWith("建议：") || line.startsWith("建议:")) {
                inSuggestions = true;
            } else if (inSuggestions && line.trim().startsWith("-")) {
                String suggestion = line.trim().substring(1).trim();
                if (!suggestion.isEmpty()) suggestions.add(suggestion);
            }
        }
        if (insight.isEmpty()) insight = "暂无智能洞察，请稍后重试。";
        if (suggestions.isEmpty()) suggestions.add("暂无具体建议。");
        report.setAiInsight(insight);
        report.setSuggestions(suggestions);
    }

    private void fallbackGenerate(MonthlyReportDTO report) {
        String insight = generateFallbackInsight(report);
        List<String> suggestions = generateFallbackSuggestions(report);
        report.setAiInsight("[本地规则] " + insight);
        report.setSuggestions(suggestions);
    }

    private String generateFallbackInsight(MonthlyReportDTO report) {
        BigDecimal income = report.getTotalIncome() == null ? BigDecimal.ZERO : report.getTotalIncome();
        BigDecimal expense = report.getTotalExpense() == null ? BigDecimal.ZERO : report.getTotalExpense();
        BigDecimal surplus = report.getSurplus() == null ? BigDecimal.ZERO : report.getSurplus();
        if (income.compareTo(BigDecimal.ZERO) == 0 && expense.compareTo(BigDecimal.ZERO) == 0)
            return "本月暂无收支记录，开始记账吧！";
        if (surplus.compareTo(BigDecimal.ZERO) < 0)
            return "本月支出超过收入，建议控制开支。";
        else if (surplus.compareTo(BigDecimal.ZERO) == 0)
            return "收支平衡，可以考虑适当储蓄。";
        else {
            BigDecimal rate = surplus.divide(income, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
            return "恭喜！本月结余率 " + rate.intValue() + "%，财务状况良好。";
        }
    }

    private List<String> generateFallbackSuggestions(MonthlyReportDTO report) {
        List<String> suggestions = new ArrayList<>();
        BigDecimal income = report.getTotalIncome() == null ? BigDecimal.ZERO : report.getTotalIncome();
        BigDecimal expense = report.getTotalExpense() == null ? BigDecimal.ZERO : report.getTotalExpense();
        if (expense.compareTo(income) > 0)
            suggestions.add("⏰ 减少非必要支出，尝试制定月度预算。");
        if (report.getExpenseRanking() != null && !report.getExpenseRanking().isEmpty()) {
            Map<String, Object> top = report.getExpenseRanking().get(0);
            suggestions.add("📊 最大支出类别为 " + top.get("categoryName") + "，可尝试优化。");
        }
        if (suggestions.isEmpty())
            suggestions.add("✨ 继续保持良好的记账习惯！");
        return suggestions;
    }
}