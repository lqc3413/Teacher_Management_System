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
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录接口
     * 
     * @param loginForm 包含 username, password 字段的 Map
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginForm) {
        String username = loginForm.get("username");
        String password = loginForm.get("password");
        String role = loginForm.get("role"); // teacher / admin

        if (username == null || password == null) {
            return Result.error("账号和密码不能为空");
        }

        // 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userService.getOne(wrapper);

        // 用户名未找到，尝试用工号登录
        if (user == null) {
            LambdaQueryWrapper<User> empWrapper = new LambdaQueryWrapper<>();
            empWrapper.eq(User::getEmployeeNo, username);
            user = userService.getOne(empWrapper);
        }

        if (user == null) {
            return Result.error("账号或密码错误");
        }

        // 校验密码（BCrypt 加密比对）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error("账号或密码错误");
        }

        // 校验角色是否匹配（admin=1, teacher=2, dept_director=3）
        if (role != null) {
            Long userRoleId = user.getRoleId();
            if ("admin".equals(role)) {
                // 管理员Tab：只允许 roleId=1
                if (!userRoleId.equals(1L)) {
                    return Result.error("该账号不是管理员角色，请切换登录方式");
                }
            } else if ("teacher".equals(role)) {
                // 教师Tab：允许 roleId=2（教师）和 roleId=3（部门主任）
                if (!userRoleId.equals(2L) && !userRoleId.equals(3L)) {
                    return Result.error("该账号不是教师角色，请切换登录方式");
                }
                // 如果是部门主任(roleId=3)，将 role 修正为 dept_director
                if (userRoleId.equals(3L)) {
                    role = "dept_director";
                }
            }
        }

        // 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userService.updateById(user);

        // 生成 JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleId(), role,
                user.getDeptId());

        // 构造返回数据（隐藏密码）
        user.setPassword(null);
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("role", role);
        data.put("token", token);

        return Result.success(data);
    }

    /** 分页查询 */
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
        // 隐藏密码字段
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    /** 根据ID查询 */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /** 新增 */
    @PostMapping
    public Result<?> save(@RequestBody User user) {
        // 校验账号非空
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("账号不能为空");
        }
        // 校验密码强度（若手动设置了密码）
        if (user.getPassword() != null && !user.getPassword().isEmpty() && user.getPassword().length() < 6) {
            return Result.error("密码长度不能少于 6 位");
        }
        // 校验账号是否已存在
        if (user.getUsername() != null) {
            long usernameCount = userService.count(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername().trim()));
            if (usernameCount > 0) {
                return Result.error("该账号已存在，请更换账号");
            }
        }
        // 校验工号是否已存在
        if (user.getEmployeeNo() != null && !user.getEmployeeNo().isEmpty()) {
            long empNoCount = userService.count(
                    new LambdaQueryWrapper<User>().eq(User::getEmployeeNo, user.getEmployeeNo().trim()));
            if (empNoCount > 0) {
                return Result.error("该工号已存在，请检查工号是否正确");
            }
        }
        // 对密码进行 BCrypt 加密
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(passwordEncoder.encode("123456"));
        }
        user.setCreatedAt(LocalDateTime.now());
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        return userService.save(user) ? Result.success() : Result.error("新增失败");
    }

    /** 修改 */
    @PutMapping
    public Result<?> update(@RequestBody User user) {
        // 如果传入了密码（管理员重置密码场景），则加密后更新
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null); // 未传密码时不修改密码字段
        }
        return userService.updateById(user) ? Result.success() : Result.error("修改失败");
    }

    /** 删除 */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        return userService.removeById(id) ? Result.success() : Result.error("删除失败");
    }

    /**
     * 修改密码 (当前登录用户)
     */
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params) {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.error("原密码和新密码不能为空");
        }
        // 校验新密码最小长度
        if (newPassword.length() < 6) {
            return Result.error("新密码长度不能少于 6 位");
        }

        Long userId = com.teacher.management.utils.SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return userService.updateById(user) ? Result.success("密码修改成功，请重新登录") : Result.error("修改失败");
    }

    /**
     * 修改个人资料 (当前登录用户)
     * 仅允许修改: 姓名, 电话, 邮箱, 性别
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User user) {
        Long currentUserId = com.teacher.management.utils.SecurityUtils.getCurrentUserId();
        User currentUser = userService.getById(currentUserId);

        if (user.getRealName() != null)
            currentUser.setRealName(user.getRealName());
        if (user.getPhone() != null)
            currentUser.setPhone(user.getPhone());
        if (user.getEmail() != null)
            currentUser.setEmail(user.getEmail());
        if (user.getGender() != null)
            currentUser.setGender(user.getGender());
        // 其他字段如工号、部门、角色不允许自修改

        return userService.updateById(currentUser) ? Result.success() : Result.error("修改失败");
    }
}
