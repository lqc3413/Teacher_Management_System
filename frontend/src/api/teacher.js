import request from '../utils/request'

/**
 * 提交当月教学科研信息
 * @param {Object} data - 符合 TeacherSubmitDTO 结构的对象
 */
export function submitTeacherInfo(data) {
    return request({
        url: '/teacher/submit',
        method: 'post',
        data
    })
}

/**
 * 查询提交历史记录
 * @param {Object} params - { year, page, size }
 */
export function getSubmissionHistory(params) {
    return request({
        url: '/teacher/history',
        method: 'get',
        params
    })
}

/**
 * 查看某次提交的详情
 * @param {Number} id - 提交记录 ID
 */
export function getSubmissionDetail(id) {
    return request({
        url: `/teacher/detail/${id}`,
        method: 'get'
    })
}

/**
 * 获取 Dashboard 统计数据
 */
export function getDashboardStats() {
    return request({
        url: '/teacher/dashboard',
        method: 'get'
    })
}

/**
 * 获取教师全部成果聚合数据
 */
export function getAchievements() {
    return request({
        url: '/teacher/achievements',
        method: 'get'
    })
}

/**
 * 获取当前进行中的采集任务
 * @returns {Promise<{data: {
 *   task: Object,                  // 当前任务对象
 *   submissionExists: boolean,     // 是否有提交记录
 *   submissionStatus: number|null, // 最新提交状态 (0=待初审,1=已归档,2=已驳回,3=待终审,4=终审退回)
 *   canResubmit: boolean,          // 是否允许重提 (status=2或4时为true)
 *   resubmitSubmissionId: number|null, // 可重提时对应的 submission ID
 *   hasSubmitted: boolean          // [过渡字段] 等价于 submissionExists，后续移除
 * }}>}
 */
export function getCurrentTask() {
    return request({
        url: '/teacher/task/current',
        method: 'get'
    })
}

/**
 * 下载 Excel 标准模板
 */
export function downloadExcelTemplate() {
    return request({
        url: '/teacher/excel/template',
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * Excel 导入数据
 * @param {FormData} formData - 包含 file 和 taskId
 */
export function importExcelData(formData) {
    return request({
        url: '/teacher/excel/import',
        method: 'post',
        data: formData,
        headers: { 'Content-Type': 'multipart/form-data' }
    })
}

/**
 * 查询当前用户学历/职称信息
 */
export function getBasicInfo() {
    return request({
        url: '/teacher/basic-info',
        method: 'get'
    })
}

/**
 * 更新当前用户学历/职称信息
 * @param {Object} data - { education, major, school, degree, degreeDate, title, isDualTeacher, skillCert }
 */
export function updateBasicInfo(data) {
    return request({
        url: '/teacher/basic-info',
        method: 'put',
        data
    })
}

/**
 * 导出个人某次提交的详情为 Excel
 * @param {Number} id - 提交记录 ID
 */
export function exportMySubmission(id) {
    return request({
        url: `/teacher/export/${id}`,
        method: 'get',
        responseType: 'blob'
    })
}

/**
 * 导出教师全部已通过的成果数据为 Excel
 */
export function exportAllAchievements() {
    return request({
        url: '/teacher/export/achievements',
        method: 'get',
        responseType: 'blob'
    })
}
