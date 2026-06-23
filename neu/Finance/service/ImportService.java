package com.neu.Finance.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface ImportService {
    // 导入账单文件，返回导入结果统计
    Map<String, Object> importBills(MultipartFile file, Long userId);
}