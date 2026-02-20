import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建 Axios 实例
const request = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json'
    }
})

// 请求拦截器 — 自动携带 JWT Token
request.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器 — 统一错误处理
request.interceptors.response.use(
    (response) => {
        // Blob 响应（文件下载）直接返回
        if (response.config.responseType === 'blob') {
            return response.data
        }
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.message || '请求失败')
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    (error) => {
        if (error.response) {
            const status = error.response.status
            if (status === 401) {
                // Token 过期或无效 — 清除登录信息，跳转到登录页
                localStorage.removeItem('token')
                localStorage.removeItem('userInfo')
                localStorage.removeItem('role')
                ElMessage.error('登录已过期，请重新登录')
                router.push('/')
            } else if (status === 403) {
                ElMessage.error('没有权限访问该资源')
            } else {
                ElMessage.error(error.response.data?.message || '服务器错误')
            }
        } else {
            ElMessage.error('网络异常，请稍后重试')
        }
        return Promise.reject(error)
    }
)

export default request
