package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.entity.User;
import com.teacher.management.service.IUserService;
import com.teacher.management.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        String frontendRole = normalizeFrontendRole(loginForm.get("role"));

        if (username == null || password == null) {
            return Result.error("\u8d26\u53f7\u548c\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userService.getOne(wrapper);

        if (user == null) {
            LambdaQueryWrapper<User> empWrapper = new LambdaQueryWrapper<>();
            empWrapper.eq(User::getEmployeeNo, username);
            user = userService.getOne(empWrapper);
        }

        if (user == null) {
            return Result.error("\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("\u8d26\u53f7\u6216\u5bc6\u7801\u9519\u8bef");
        }

        Long userRoleId = user.getRoleId();
        String role;
        if (Objects.equals(userRoleId, 1L)) {
            role = "admin";
        } else if (Objects.equals(userRoleId, 3L)) {
            role = "dept_director";
        } else {
            role = "teacher";
        }

        if (frontendRole != null) {
            if (!"admin".equals(frontendRole)
                    && !"teacher".equals(frontendRole)
                    && !"dept_director".equals(frontendRole)) {
                return Result.error(400, "\u65e0\u6548\u7684\u767b\u5f55\u89d2\u8272\u53c2\u6570");
            }
            if ("admin".equals(frontendRole) && !Objects.equals(userRoleId, 1L)) {
                return Result.error("\u8be5\u8d26\u53f7\u4e0d\u662f\u7ba1\u7406\u5458\u89d2\u8272\uff0c\u8bf7\u5207\u6362\u767b\u5f55\u65b9\u5f0f");
            }
            if ("teacher".equals(frontendRole)
                    && !Objects.equals(userRoleId, 2L)
                    && !Objects.equals(userRoleId, 3L)) {
                return Result.error("\u8be5\u8d26\u53f7\u4e0d\u662f\u6559\u5e08\u89d2\u8272\uff0c\u8bf7\u5207\u6362\u767b\u5f55\u65b9\u5f0f");
            }
            if ("dept_director".equals(frontendRole) && !Objects.equals(userRoleId, 3L)) {
                return Result.error("\u8be5\u8d26\u53f7\u4e0d\u662f\u90e8\u95e8\u4e3b\u4efb\u89d2\u8272\uff0c\u8bf7\u5207\u6362\u767b\u5f55\u65b9\u5f0f");
            }
        }

        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("\u8d26\u53f7\u5df2\u88ab\u7981\u7528");
        }

        user.setLastLogin(LocalDateTime.now());
        userService.updateById(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleId(), role,
                user.getDeptId());

        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("role", role);
        data.put("token", token);

        return Result.success(data);
    }

    private String normalizeFrontendRole(String frontendRole) {
        if (frontendRole == null) {
            return null;
        }
        String normalized = frontendRole.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? null : normalized;
    }

    @GetMapping("/list")
    public Result<?> list(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(required = false) Long deptId) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(realName != null, User::getRealName, realName);
        wrapper.eq(employeeNo != null, User::getEmployeeNo, employeeNo);
        wrapper.eq(deptId != null, User::getDeptId, deptId);
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userService.page(page, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    @GetMapping("/me")
    public Result<?> getCurrentUser() {
        Long userId = com.teacher.management.utils.SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error("\u672a\u767b\u5f55");
        }
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PostMapping
    public Result<?> save(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("\u8d26\u53f7\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() < 6) {
            return Result.error("\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e 6 \u4f4d");
        }
        if (user.getUsername() != null) {
            long usernameCount = userService.count(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername().trim()));
            if (usernameCount > 0) {
                return Result.error("\u8be5\u8d26\u53f7\u5df2\u5b58\u5728\uff0c\u8bf7\u66f4\u6362\u8d26\u53f7");
            }
        }
        if (user.getEmployeeNo() != null && !user.getEmployeeNo().isEmpty()) {
            long empNoCount = userService.count(
                    new LambdaQueryWrapper<User>().eq(User::getEmployeeNo, user.getEmployeeNo().trim()));
            if (empNoCount > 0) {
                return Result.error("\u8be5\u5de5\u53f7\u5df2\u5b58\u5728\uff0c\u8bf7\u68c0\u67e5\u5de5\u53f7\u662f\u5426\u6b63\u786e");
            }
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        user.setCreatedAt(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        return userService.save(user) ? Result.success() : Result.error("\u65b0\u589e\u5931\u8d25");
    }

    @PutMapping
    public Result<?> update(@RequestBody User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        return userService.updateById(user) ? Result.success() : Result.error("\u4fee\u6539\u5931\u8d25");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return userService.removeById(id) ? Result.success() : Result.error("\u5220\u9664\u5931\u8d25");
    }

    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("\u539f\u5bc6\u7801\u548c\u65b0\u5bc6\u7801\u4e0d\u80fd\u4e3a\u7a7a");
        }
        if (newPassword.length() < 6) {
            return Result.error("\u65b0\u5bc6\u7801\u957f\u5ea6\u4e0d\u80fd\u5c11\u4e8e 6 \u4f4d");
        }

        Long userId = com.teacher.management.utils.SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("\u539f\u5bc6\u7801\u9519\u8bef");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user)
                ? Result.success("\u5bc6\u7801\u4fee\u6539\u6210\u529f\uff0c\u8bf7\u91cd\u65b0\u767b\u5f55")
                : Result.error("\u4fee\u6539\u5931\u8d25");
    }

    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User user) {
        Long currentUserId = com.teacher.management.utils.SecurityUtils.getCurrentUserId();
        User currentUser = userService.getById(currentUserId);

        if (user.getRealName() != null) {
            currentUser.setRealName(user.getRealName());
        }
        if (user.getPhone() != null) {
            currentUser.setPhone(user.getPhone());
        }
        if (user.getEmail() != null) {
            currentUser.setEmail(user.getEmail());
        }
        if (user.getGender() != null) {
            currentUser.setGender(user.getGender());
        }

        return userService.updateById(currentUser) ? Result.success() : Result.error("\u4fee\u6539\u5931\u8d25");
    }
}
