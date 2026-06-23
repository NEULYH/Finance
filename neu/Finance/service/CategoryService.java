package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.Category;
import java.util.List;

public interface CategoryService extends IService<Category> {
    List<Category> getListByType(Long userId, String type);
    boolean addCategory(Category category);
    boolean updateCategory(Category category);
    boolean deleteCategory(Long id, Long userId);
    List<Category> getUserCategories(Long userId, String type);
}