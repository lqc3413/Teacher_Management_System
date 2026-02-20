import request from '../utils/request'

// ========== 消息通知 ==========

/**
 * 获取当前用户消息列表
 * @param {Object} params - { pageNum, pageSize }
 */
export function getMessages(params) {
    return request({
        url: '/messages',
        method: 'get',
        params
    })
}

/**
 * 获取未读消息数量
 */
export function getUnreadCount() {
    return request({
        url: '/messages/unread-count',
        method: 'get'
    })
}

/**
 * 标记单条消息为已读
 * @param {Number} id - 消息ID
 */
export function markAsRead(id) {
    return request({
        url: `/messages/${id}/read`,
        method: 'put'
    })
}

/**
 * 标记所有消息为已读
 */
export function markAllAsRead() {
    return request({
        url: '/messages/read-all',
        method: 'put'
    })
}
