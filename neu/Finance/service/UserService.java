package com.neu.Finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.neu.Finance.entity.User;
import jakarta.servlet.http.HttpSession;

public interface UserService extends IService<User> {
    // 登录方法，返回登录成功的用户对象，并存入 session
    User login(String username, String password, HttpSession session);
    
    boolean register(User user);
    
    boolean updateProfile(Long userId, String phone, String email);
    
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}