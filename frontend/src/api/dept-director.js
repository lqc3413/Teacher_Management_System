import request from '../utils/request'

// 获取部门主任待审核列表
export function getDeptPendingSubmissions(params) {
    return request.get('/dept-director/pending', { params })
}

// 部门主任审核操作
export function deptAuditSubmission(id, data) {
    return request.put(`/dept-director/audit/${id}`, data)
}

// 获取提交详情
export function getDeptSubmissionDetail(id) {
    return request.get(`/dept-director/submission/${id}`)
}

// 获取部门主任仪表盘统计
export function getDeptDirectorStats() {
    return request.get('/dept-director/stats')
}
