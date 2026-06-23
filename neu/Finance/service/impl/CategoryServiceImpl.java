package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.entity.Category;
import com.neu.Finance.mapper.CategoryMapper;
import com.neu.Finance.service.CategoryService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Override
    public List<Category> getListByType(Long userId, String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId)
               .eq(Category::getType, type)
               .orderByAsc(Category::getParentId)
               .orderByAsc(Category::getName);
        return this.list(wrapper);
    }
    @Override
    public List<Category> getUserCategories(Long userId, String type) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Category::getUserId, userId);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Category::getType, type);
        }
        wrapper.orderByDesc(Category::getCreateTime);
        return this.list(wrapper);
    }
    @Override
    public boolean addCategory(Category category) {
        // 可添加去重校验（同一用户下同一类型下名称不能重复）
        return this.save(category);
    }

    @Override
    public boolean updateCategory(Category category) {
        return this.updateById(category);
    }

    @Override
    public boolean deleteCategory(Long id, Long userId) {
        // 检查是否有账单使用该分类，可在这里添加校验（省略）
        return this.removeById(id);
    }
}