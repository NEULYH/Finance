package com.neu.Finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neu.Finance.entity.User;
import com.neu.Finance.mapper.UserMapper;
import com.neu.Finance.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private String encryptPassword(String password) {
        return DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public User login(String username, String password, HttpSession session) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username).or().eq(User::getPhone, username);
        User user = this.getOne(wrapper);
        if (user == null) return null;
        if (!user.getPassword().equals(encryptPassword(password))) return null;

        // 存入 session（有效期默认30分钟，可在配置文件中修改）
        session.setAttribute("userId", user.getId());
        session.setAttribute("user", user);
        return user;
    }

    @Override
    public boolean register(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (this.count(wrapper) > 0) return false;
        user.setPassword(encryptPassword(user.getPassword()));
        return this.save(user);
    }

    @Override
    public boolean updateProfile(Long userId, String phone, String email) {
        User user = new User();
        user.setId(userId);
        user.setPhone(phone);
        user.setEmail(email);
        return this.updateById(user);
    }

    @Override
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) return false;
        if (!user.getPassword().equals(encryptPassword(oldPassword))) return false;
        user.setPassword(encryptPassword(newPassword));
        return this.updateById(user);
    }
}