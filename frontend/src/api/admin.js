import request from '../utils/request'

// ========== 用户管理 ==========

/**
 * 获取用户列表
 * @param {Object} params - { pageNum, pageSize, realName, employeeNo, deptId }
 */
export function getUserList(params) {
    return request({
        url: '/users/list',
        method: 'get',
        params
    })
}

/**
 * 新增用户
 * @param {Object} data 
 */
export function addUser(data) {
    return request({
        url: '/users',
        method: 'post',
        data
    })
}

/**
 * 修改用户
 * @param {Object} data 
 */
export function updateUser(data) {
    return request({
        url: '/users',
        method: 'put',
        data
    })
}

/**
 * 删除用户
 * @param {Number} id 
 */
export function deleteUser(id) {
    return request({
        url: `/users/${id}`,
        method: 'delete'
    })
}

/**
 * 重置密码 (通常是更新用户接口的一部分，或者单独接口，这里假设用 updateUser)
 * 暂时前端传 password 字段调用 updateUser
 */


// ========== 部门管理 ==========

export function getDeptList(params) {
    return request({
        url: '/departments/list',
        method: 'get',
        params
    })
}

export function getAllDepts() {
    return request({
        url: '/departments/all',
        method: 'get'
    })
}

export function addDept(data) {
    return request({
        url: '/departments',
        method: 'post',
        data
    })
}

export function updateDept(data) {
    return request({
        url: '/departments',
        method: 'put',
        data
    })
}

export function deleteDept(id) {
    return request({
        url: `/departments/${id}`,
        method: 'delete'
    })
}

export function getDeptMembers(deptId) {
    return request({
        url: `/departments/${deptId}/members`,
        method: 'get'
    })
}

// ========== 管理员仪表盘 ==========

export function getAdminStats() {
    return request({
        url: '/admin/stats',
        method: 'get'
    })
}

// ========== 提交审核（管理员） ==========

/**
 * 获取所有教师提交记录
 * @param {Object} params - { pageNum, pageSize, status, deptId }
 */
export function getSubmissionList(params) {
    return request({
        url: '/admin/submissions',
        method: 'get',
        params
    })
}

/**
 * 查看提交详情
 * @param {Number} id
 */
export function getSubmissionDetail(id) {
    return request({
        url: `/admin/submissions/detail/${id}`,
        method: 'get'
    })
}

/**
 * 审核提交记录
 * @param {Object} data - { id, status, auditRemark }
 */
export function auditSubmission(data) {
    return request({
        url: `/admin/submissions/audit/${data.id}`,
        method: 'put',
        params: {
            status: data.status,
            auditRemark: data.auditRemark
        }
    })
}

// ========== 系统配置 ==========

export function getConfigList() {
    return request({
        url: '/config/list',
        method: 'get'
    })
}

export function updateConfigBatch(data) {
    return request({
        url: '/config/batch',
        method: 'put',
        data
    })
}

// ========== 通知公告 ==========

export function getNoticeList(params) {
    return request({
        url: '/notices/list',
        method: 'get',
        params
    })
}

export function getPublishedNotices() {
    return request({
        url: '/notices/published',
        method: 'get'
    })
}

export function addNotice(data) {
    return request({
        url: '/notices',
        method: 'post',
        data
    })
}

export function updateNotice(data) {
    return request({
        url: '/notices',
        method: 'put',
        data
    })
}

export function deleteNotice(id) {
    return request({
        url: `/notices/${id}`,
        method: 'delete'
    })
}

// ========== 采集任务管理 ==========

export function getTaskList(params) {
    return request({
        url: '/admin/task/list',
        method: 'get',
        params
    })
}

export function createTask(data) {
    return request({
        url: '/admin/task/create',
        method: 'post',
        data
    })
}

export function publishTask(id) {
    return request({
        url: `/admin/task/publish/${id}`,
        method: 'put'
    })
}

export function endTask(id) {
    return request({
        url: `/admin/task/end/${id}`,
        method: 'put'
    })
}

export function deleteTask(id) {
    return request({
        url: `/admin/task/delete/${id}`,
        method: 'delete'
    })
}

/**
 * 批量导出提交数据
 * @param {Object} params - { status, deptId }
 */
export function exportSubmissions(params) {
    return request({
        url: '/admin/export',
        method: 'get',
        params,
        responseType: 'blob'
    })
}

/**
 * 导出单条提交详情
 * @param {Number} id - 提交记录 ID
 */
export function exportSingleSubmission(id) {
    return request({
        url: `/admin/export/${id}`,
        method: 'get',
        responseType: 'blob'
    })
}
