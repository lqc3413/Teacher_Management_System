import request from '../utils/request'

/**
 * 用户登录
 * @param {Object} data - { username, password, role }
 */
export function login(data) {
    return request({
        url: '/users/login',
        method: 'post',
        data
    })
}

/**
 * 获取用户信息
 * @param {Number} id
 */
export function getUserInfo(id) {
    return request({
        url: `/users/${id}`,
        method: 'get'
    })
}

/**
 * 获取当前登录用户自己的信息（所有角色可用）
 */
export function getCurrentUserInfo() {
    return request({
        url: '/users/me',
        method: 'get'
    })
}

/**
 * 教师注册
 * @param {Object} data - { username, password, confirmPassword, realName, employeeNo }
 */
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

/**
 * 修改密码
 * @param {Object} data - { oldPassword, newPassword }
 */
export function updatePassword(data) {
    return request({
        url: '/users/password',
        method: 'put',
        data
    })
}

/**
 * 修改个人资料
 * @param {Object} data - { realName, phone, email, gender }
 */
export function updateProfile(data) {
    return request({
        url: '/users/profile',
        method: 'put',
        data
    })
}
