package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.management.common.Result;
import com.teacher.management.dto.RegisterDTO;
import com.teacher.management.entity.User;
import com.teacher.management.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 认证控制器（注册等无需登录的操作）
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 教师注册
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        // 1. 基本校验
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            return Result.error("密码长度不能少于6位");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            return Result.error("两次密码输入不一致");
        }
        if (dto.getRealName() == null || dto.getRealName().trim().isEmpty()) {
            return Result.error("真实姓名不能为空");
        }
        if (dto.getEmployeeNo() == null || dto.getEmployeeNo().trim().isEmpty()) {
            return Result.error("教职工号不能为空");
        }

        // 2. 查重：用户名
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, dto.getUsername().trim());
        if (userService.count(usernameQuery) > 0) {
            return Result.error("该用户名已被注册");
        }

        // 3. 查重：教职工号
        LambdaQueryWrapper<User> empNoQuery = new LambdaQueryWrapper<>();
        empNoQuery.eq(User::getEmployeeNo, dto.getEmployeeNo().trim());
        if (userService.count(empNoQuery) > 0) {
            return Result.error("该教职工号已被注册");
        }

        // 4. 组装用户实体 & 入库
        User user = new User();
        user.setUsername(dto.getUsername().trim());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName().trim());
        user.setEmployeeNo(dto.getEmployeeNo().trim());
        user.setRoleId(2L);       // 教师角色 ID = 2
        user.setStatus(1);        // 1 = 启用
        user.setCreatedAt(LocalDateTime.now());

        boolean saved = userService.save(user);
        if (!saved) {
            return Result.error("注册失败，请重试");
        }

        return Result.success("注册成功，请登录");
    }
}
