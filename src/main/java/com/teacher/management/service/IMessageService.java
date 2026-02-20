package com.teacher.management.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.teacher.management.entity.Message;

public interface IMessageService extends IService<Message> {

    /**
     * 发送一条消息
     */
    void sendMessage(Integer type, Long senderId, Long receiverId, Long relatedId, String title, String content);

    /**
     * 给所有管理员发送消息
     */
    void sendToAllAdmins(Integer type, Long senderId, Long relatedId, String title, String content);

    /**
     * 分页查询某用户的消息列表
     */
    Page<Message> getMyMessages(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记单条消息为已读
     */
    void markAsRead(Long messageId, Long userId);

    /**
     * 标记该用户所有消息为已读
     */
    void markAllAsRead(Long userId);
}
