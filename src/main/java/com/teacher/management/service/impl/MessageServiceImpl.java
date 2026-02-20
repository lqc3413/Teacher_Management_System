package com.teacher.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.teacher.management.entity.Message;
import com.teacher.management.entity.User;
import com.teacher.management.mapper.MessageMapper;
import com.teacher.management.mapper.UserMapper;
import com.teacher.management.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendMessage(Integer type, Long senderId, Long receiverId, Long relatedId, String title, String content) {
        Message msg = new Message();
        msg.setType(type);
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setRelatedId(relatedId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setIsRead(0);
        msg.setCreatedAt(LocalDateTime.now());
        this.save(msg);
    }

    @Override
    public void sendToAllAdmins(Integer type, Long senderId, Long relatedId, String title, String content) {
        // 查询所有管理员 (roleId = 1)
        QueryWrapper<User> query = new QueryWrapper<>();
        query.eq("role_id", 1).eq("status", 1);
        List<User> admins = userMapper.selectList(query);

        for (User admin : admins) {
            sendMessage(type, senderId, admin.getId(), relatedId, title, content);
        }
    }

    @Override
    public Page<Message> getMyMessages(Long userId, Integer pageNum, Integer pageSize) {
        Page<Message> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("receiver_id", userId).orderByDesc("created_at");
        return this.page(page, query);
    }

    @Override
    public long getUnreadCount(Long userId) {
        QueryWrapper<Message> query = new QueryWrapper<>();
        query.eq("receiver_id", userId).eq("is_read", 0);
        return this.count(query);
    }

    @Override
    public void markAsRead(Long messageId, Long userId) {
        UpdateWrapper<Message> update = new UpdateWrapper<>();
        update.eq("id", messageId).eq("receiver_id", userId).set("is_read", 1);
        this.update(update);
    }

    @Override
    public void markAllAsRead(Long userId) {
        UpdateWrapper<Message> update = new UpdateWrapper<>();
        update.eq("receiver_id", userId).eq("is_read", 0).set("is_read", 1);
        this.update(update);
    }
}
