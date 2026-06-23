package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.Category;
import com.neu.Finance.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list(@RequestParam(required = false) String type, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        List<Category> list = categoryService.getUserCategories(userId, type);
        return Result.success(list);
    }

    @PostMapping
    public Result<String> add(@RequestBody Category category, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        category.setUserId(userId);
        boolean ok = categoryService.save(category);
        return ok ? Result.success("添加成功") : Result.error("添加失败");
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Category category, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        category.setId(id);
        category.setUserId(userId);
        boolean ok = categoryService.updateById(category);
        return ok ? Result.success("修改成功") : Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        boolean ok = categoryService.removeById(id);
        return ok ? Result.success("删除成功") : Result.error("删除失败");
    }
}