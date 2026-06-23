package com.neu.Finance.controller;

import com.neu.Finance.common.Result;
import com.neu.Finance.entity.User;
import com.neu.Finance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> params, HttpSession session) {
        String username = params.get("username");
        String password = params.get("password");
        User user = userService.login(username, password, session);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        boolean ok = userService.register(user);
        if (ok) {
            return Result.success("注册成功");
        }
        return Result.error("用户名已存在");
    }

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/profile")
    public Result<String> updateProfile(@RequestBody User user, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean ok = userService.updateProfile(userId, user.getPhone(), user.getEmail());
        return ok ? Result.success("更新成功") : Result.error("更新失败");
    }

    @PutMapping("/password")
    public Result<String> updatePassword(@RequestBody Map<String, String> params, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean ok = userService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
        return ok ? Result.success("密码修改成功") : Result.error("原密码错误");
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpSession session) {
        session.invalidate();
        return Result.success("退出成功");
    }
}