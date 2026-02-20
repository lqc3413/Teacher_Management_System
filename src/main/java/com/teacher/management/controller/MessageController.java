package com.teacher.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.management.common.Result;
import com.teacher.management.entity.Message;
import com.teacher.management.entity.User;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知接口
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取当前用户的消息列表（分页）
     */
    @GetMapping
    public Result<?> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户不存在");
        }

        Page<Message> page = messageService.getMyMessages(userId, pageNum, pageSize);

        Map<String, Object> data = new HashMap<>();
        data.put("records", page.getRecords());
        data.put("total", page.getTotal());
        return Result.success(data);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Result<?> unreadCount() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户不存在");
        }

        long count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记单条消息为已读
     */
    @PutMapping("/{id}/read")
    public Result<?> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户不存在");
        }

        messageService.markAsRead(id, userId);
        return Result.success();
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<?> markAllAsRead() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.error("用户不存在");
        }

        messageService.markAllAsRead(userId);
        return Result.success();
    }

    /**
     * 从 SecurityContext 获取当前登录用户 ID
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        String username = auth.getName();
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("username", username);
        User user = userMapper.selectOne(query);
        return user != null ? user.getId() : null;
    }
}
