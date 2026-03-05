<template>
  <div class="login-container teacher-theme">
    <div class="login-card">
      <div class="login-header">
        <div class="logo-area">
          <el-icon :size="48" color="var(--color-primary)"><School /></el-icon>
        </div>
        <h2>高校教学科研管理系统</h2>
        <p>Teaching & Research Management Platform</p>
      </div>

      <el-tabs v-model="activeRole" class="login-tabs" stretch>
        <el-tab-pane label="教师登录" name="teacher">
          <el-form 
            ref="teacherFormRef"
            :model="teacherForm"
            :rules="loginRules"
            label-position="top"
            size="large"
            class="login-form"
          >
            <el-form-item label="工号 / 账号" prop="username">
              <el-input 
                v-model="teacherForm.username" 
                placeholder="请输入工号" 
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="teacherForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin('teacher')"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-btn" 
                :loading="loading"
                @click="handleLogin('teacher')"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="管理员登录" name="admin">
          <el-form 
            ref="adminFormRef"
            :model="adminForm"
            :rules="loginRules"
            label-position="top"
            size="large"
            class="login-form"
          >
            <el-form-item label="管理员账号" prop="username">
              <el-input 
                v-model="adminForm.username" 
                placeholder="请输入管理员账号" 
                prefix-icon="User"
              />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="adminForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin('admin')"
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                class="login-btn" 
                :loading="loading"
                @click="handleLogin('admin')"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>


      
      <div class="login-footer">
        <p>© 2026 Teaching & Research Management System. All rights reserved.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock, School } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '../api/user'

const router = useRouter()
const activeRole = ref('teacher')
const loading = ref(false)


// -------- 登录 --------
const teacherFormRef = ref(null)
const adminFormRef = ref(null)
const teacherForm = reactive({ username: '', password: '' })
const adminForm = reactive({ username: '', password: '' })

const loginRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于 6 位', trigger: 'blur' }
  ]
}

const handleLogin = async (role) => {
  const formRef = role === 'teacher' ? teacherFormRef.value : adminFormRef.value
  const formData = role === 'teacher' ? teacherForm : adminForm

  if (!formRef) return

  await formRef.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请检查输入项')
      return
    }

    loading.value = true
    try {
      const res = await login({
        username: formData.username,
        password: formData.password,
        role: role
      })

      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userInfo', JSON.stringify(res.data.user))
      localStorage.setItem('role', res.data.role)
      
      ElMessage.success('登录成功！')
      const actualRole = res.data.role
      const dashboardMap = { admin: '/admin/dashboard', dept_director: '/dept-director/dashboard' }
      router.push(dashboardMap[actualRole] || '/teacher/dashboard')
    } catch (error) {
      console.error('登录失败:', error)
    } finally {
      loading.value = false
    }
  })
}


</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at 50% -20%, #F5F3FF 0%, var(--color-bg) 100%);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01);
  border: 1px solid rgba(124, 58, 237, 0.05);
  transition: transform 0.3s ease;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo-area {
  margin-bottom: 16px;
}

.login-header h2 {
  margin: 0;
  color: var(--color-text);
  font-family: var(--font-heading);
  font-size: 32px;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.login-header p {
  margin: 8px 0 0;
  color: #6B7280;
  font-family: var(--font-body);
  font-size: 16px;
}

.login-form .el-input {
  --el-input-height: 44px;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  background-color: var(--color-cta);
  border-color: var(--color-cta);
  transition: all 0.2s;
  cursor: pointer;
}

.login-btn:hover {
  background-color: #EA580C;
  border-color: #EA580C;
  transform: translateY(-1px);
  box-shadow: 0 4px 6px -1px rgba(249, 115, 22, 0.2);
}

.login-footer {
  margin-top: 32px;
  text-align: center;
  border-top: 1px solid #F3F4F6;
  padding-top: 20px;
}

.login-footer p {
  color: #9CA3AF;
  font-size: 12px;
  margin: 0;
}

/* 覆盖 Element Plus Tabs 样式 */
:deep(.el-tabs__nav-wrap::after) {
  height: 2px;
  background-color: #F3F4F6;
}
:deep(.el-tabs__active-bar) {
  background-color: var(--color-primary);
  height: 2px;
}
:deep(.el-tabs__item) {
  font-family: var(--font-heading);
  font-size: 18px;
  color: #6B7280;
  font-weight: 500;
  cursor: pointer;
}
:deep(.el-tabs__item.is-active) {
  color: var(--color-primary);
  font-weight: 600;
}

/* 响应式适配 */
@media screen and (max-width: 768px) {
  .login-card {
    padding: 32px 24px;
    box-shadow: none;
    border: none;
    background: transparent;
  }
  
  .login-container {
    background: white;
    align-items: flex-start;
    padding-top: 40px;
  }

  .login-header h2 {
    font-size: 28px;
  }
}
</style>
