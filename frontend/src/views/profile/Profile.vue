<template>
  <div class="profile-container">
    <el-row :gutter="24">
      <!-- 左侧：个人信息卡片 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="7">
        <el-card class="profile-card" shadow="hover" :body-style="{ padding: '0px' }">
          <div class="card-header-bg"></div>
          <div class="profile-content">
            <div class="avatar-wrapper">
              <el-avatar :size="100" class="avatar">{{ userInitials }}</el-avatar>
            </div>
            <h2 class="username">{{ infoForm.realName }}</h2>
            <div class="role-badge">
              <el-tag effect="dark" :type="currentRole === 'admin' ? 'primary' : 'success'" round>{{ currentRole === 'admin' ? '管理员' : '教师' }}</el-tag>
            </div>
            
            <el-divider />
            
            <div class="info-list">
              <div class="info-item">
                <el-icon><User /></el-icon>
                <span class="label">工号:</span>
                <span class="value">{{ infoForm.employeeNo || '未分配' }}</span>
              </div>
              <div class="info-item">
                <el-icon><School /></el-icon>
                <span class="label">部门:</span>
                <span class="value">{{ infoForm.deptName || '暂无数据' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Timer /></el-icon>
                <span class="label">注册:</span>
                <span class="value">{{ formatDate(infoForm.createdAt) }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card class="welcome-card" shadow="hover">
          <div class="welcome-content">
            <p>🌟 欢迎回来，每一天都是新的开始！</p>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：设置面板 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="17">
        <el-card class="settings-card" shadow="hover">
          <template #header>
            <div class="settings-header">
              <span class="title">账号设置</span>
              <span class="subtitle">管理您的个人资料和安全设置</span>
            </div>
          </template>

          <el-tabs v-model="activeTab" class="profile-tabs">
            <!-- 个人资料 -->
            <el-tab-pane label="基本资料" name="info">
              <el-form 
                ref="infoFormRef"
                :model="infoForm"
                :rules="infoRules"
                label-width="100px"
                class="settings-form"
                label-position="top"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="姓名" prop="realName">
                      <el-input v-model="infoForm.realName" placeholder="请输入姓名">
                        <template #prefix><el-icon><User /></el-icon></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                     <el-form-item label="性别" prop="gender">
                      <el-radio-group v-model="infoForm.gender">
                        <el-radio :value="1" border>男</el-radio>
                        <el-radio :value="2" border>女</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="手机号码" prop="phone">
                      <el-input v-model="infoForm.phone" placeholder="请输入手机号" maxlength="11">
                        <template #prefix><el-icon><Iphone /></el-icon></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="电子邮箱" prop="email">
                      <el-input v-model="infoForm.email" placeholder="请输入邮箱">
                        <template #prefix><el-icon><Message /></el-icon></template>
                      </el-input>
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="handleUpdateInfo" size="large" class="save-btn">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 修改密码 -->
            <el-tab-pane label="安全设置" name="password">
              <el-alert
                title="为了您的账户安全，建议定期更换密码"
                type="info"
                show-icon
                :closable="false"
                class="pwd-alert"
              />
              <el-form
                ref="pwdFormRef"
                :model="pwdForm"
                :rules="pwdRules"
                label-width="100px"
                class="settings-form"
                label-position="top"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input 
                    v-model="pwdForm.oldPassword" 
                    type="password" 
                    show-password 
                    placeholder="请输入原密码"
                  />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input 
                    v-model="pwdForm.newPassword" 
                    type="password" 
                    show-password 
                    placeholder="长度在 6 到 20 个字符"
                  />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="pwdForm.confirmPassword" 
                    type="password" 
                    show-password 
                    placeholder="请再次输入新密码"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="loading" @click="handleUpdatePwd" size="large" class="save-btn">确认修改</el-button>
                  <el-button @click="resetPwdForm" size="large">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>

            <!-- 学历职称（仅教师角色显示） -->
            <el-tab-pane v-if="currentRole === 'teacher'" label="学历职称" name="education">
              <el-form
                ref="eduFormRef"
                :model="eduForm"
                label-width="120px"
                class="settings-form"
                label-position="top"
              >
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="最高学历">
                      <el-input v-model="eduForm.education" placeholder="如：博士研究生" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="所学专业">
                      <el-input v-model="eduForm.major" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="毕业院校">
                      <el-input v-model="eduForm.school" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="最高学位">
                      <el-input v-model="eduForm.degree" placeholder="如：工学博士" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="取得学位时间">
                      <el-date-picker
                        v-model="eduForm.degreeDate"
                        type="month"
                        placeholder="选择月份"
                        style="width: 100%"
                        value-format="YYYY-MM"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="专业技术职称">
                      <el-input v-model="eduForm.title" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-form-item label="是否双师型">
                      <el-radio-group v-model="eduForm.isDualTeacher">
                        <el-radio :value="1" border>是</el-radio>
                        <el-radio :value="0" border>否</el-radio>
                      </el-radio-group>
                    </el-form-item>
                  </el-col>
                  <el-col :span="12">
                    <el-form-item label="职业技能证书名称">
                      <el-input v-model="eduForm.skillCert" />
                    </el-form-item>
                  </el-col>
                </el-row>

                <el-form-item>
                  <el-button type="primary" :loading="eduLoading" @click="handleUpdateEdu" size="large" class="save-btn">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, School, Timer, Iphone, Message } from '@element-plus/icons-vue'
import { getCurrentUserInfo, updateProfile, updatePassword } from '../../api/user'
import { getBasicInfo, updateBasicInfo } from '../../api/teacher'
import { getAllDepts } from '../../api/admin'
import dayjs from 'dayjs'

const activeTab = ref('info')
const loading = ref(false)
const infoFormRef = ref(null)
const pwdFormRef = ref(null)
const eduFormRef = ref(null)
const deptMap = ref({})
const currentRole = ref(localStorage.getItem('role') || 'teacher')

// 个人资料
const infoForm = reactive({
  id: '',
  username: '',
  realName: '用户',
  employeeNo: '',
  gender: 1,
  phone: '',
  email: '',
  deptId: '',
  deptName: '',
  createdAt: ''
})

const userInitials = computed(() => {
  return infoForm.realName ? infoForm.realName.charAt(0) : 'U'
})

const infoRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
}

// 修改密码
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== pwdForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [{ validator: validatePass2, trigger: 'blur' }]
}

const formatDate = (date) => {
  if (!date) return '未知'
  return dayjs(date).format('YYYY-MM-DD')
}

// 初始化数据
const initData = async () => {
  try {
    const userInfoStr = localStorage.getItem('userInfo')
    if (!userInfoStr) return
    const localUser = JSON.parse(userInfoStr)
    
    // 获取最新用户信息（通过 /api/users/me，无需传 id）
    const res = await getCurrentUserInfo()
    const user = res.data
    
    Object.assign(infoForm, {
      id: user.id,
      username: user.username,
      realName: user.realName,
      employeeNo: user.employeeNo,
      gender: user.gender || 1,
      phone: user.phone,
      email: user.email,
      deptId: user.deptId,
      createdAt: user.createdAt
    })

    // Fetch Deuts
    const deptRes = await getAllDepts()
    if (deptRes.data) {
      const dept = deptRes.data.find(d => d.id === user.deptId)
      if (dept) {
        infoForm.deptName = dept.name
      }
    }

  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

onMounted(() => {
  initData()
  // 学历职称仅教师角色加载
  if (currentRole.value === 'teacher') {
    loadEduInfo()
  }
})

// 学历职称
const eduLoading = ref(false)
const eduForm = reactive({
  education: '',
  major: '',
  school: '',
  degree: '',
  degreeDate: '',
  title: '',
  isDualTeacher: 0,
  skillCert: ''
})

const loadEduInfo = async () => {
  try {
    const res = await getBasicInfo()
    if (res.data) {
      Object.assign(eduForm, {
        education: res.data.education || '',
        major: res.data.major || '',
        school: res.data.school || '',
        degree: res.data.degree || '',
        degreeDate: res.data.degreeDate || '',
        title: res.data.title || '',
        isDualTeacher: res.data.isDualTeacher || 0,
        skillCert: res.data.skillCert || ''
      })
    }
  } catch (e) {
    console.error('获取学历信息失败', e)
  }
}

const handleUpdateEdu = async () => {
  eduLoading.value = true
  try {
    await updateBasicInfo(eduForm)
    ElMessage.success('学历/职称信息更新成功')
  } catch (e) {
    ElMessage.error('更新失败：' + (e.message || '未知错误'))
  } finally {
    eduLoading.value = false
  }
}

// 更新资料
const handleUpdateInfo = () => {
  infoFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updateProfile(infoForm)
        ElMessage.success('资料修改成功')
        // 更新本地存储
        const userInfo = JSON.parse(localStorage.getItem('userInfo'))
        userInfo.realName = infoForm.realName
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
      } finally {
        loading.value = false
      }
    }
  })
}

// 更新密码
const handleUpdatePwd = () => {
  pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await updatePassword({
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        localStorage.clear()
        setTimeout(() => {
          window.location.reload()
        }, 1500)
      } finally {
        loading.value = false
      }
    }
  })
}

const resetPwdForm = () => {
  pwdFormRef.value.resetFields()
}
</script>

<style scoped lang="scss">
.profile-container {
  max-width: 1280px;
  margin: 0 auto;
}

.profile-card {
  position: relative;
  text-align: center;
  overflow: hidden;
  margin-bottom: 20px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-sm);

  .card-header-bg {
    height: 120px;
    background: var(--profile-header-bg);
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 40px;
      background: linear-gradient(180deg, transparent, rgba(255,255,255,0.1));
    }
  }

  .profile-content {
    position: relative;
    padding: 0 24px 24px;
    margin-top: -50px; 

    .avatar-wrapper {
      margin-bottom: 16px;
      .avatar {
        border: 4px solid #fff;
        background: var(--avatar-bg);
        color: var(--avatar-color);
        font-size: 36px;
        font-weight: 700;
        font-family: var(--font-heading);
        box-shadow: 0 4px 12px rgba(79, 70, 229, 0.15);
      }
    }

    .username {
      margin: 0 0 8px;
      font-size: 20px;
      color: var(--color-text);
      font-weight: 600;
      font-family: var(--font-heading);
      letter-spacing: -0.01em;
    }

    .role-badge {
      margin-bottom: 24px;
    }

    .info-list {
      text-align: left;
      
      .info-item {
        display: flex;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid var(--color-border);
        color: var(--color-text-light);
        font-size: 14px;
        font-family: var(--font-body);

        &:last-child {
          border-bottom: none;
        }

        .el-icon {
          margin-right: 12px;
          font-size: 16px;
          color: var(--color-secondary);
        }

        .label {
          width: 50px;
          color: var(--color-text-light);
          font-weight: 500;
        }
        
        .value {
          font-weight: 500;
          color: var(--color-text);
        }
      }
    }
  }
}

.welcome-card {
  background: #F8FAFC;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  margin-bottom: 20px;
  box-shadow: var(--shadow-sm);
  
  .welcome-content {
    text-align: center;
    color: var(--color-text);
    font-weight: 500;
    font-family: var(--font-heading);
  }
}

.settings-card {
  min-height: 500px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-card);
  box-shadow: var(--shadow-sm);
  
  :deep(.el-card__header) {
    border-bottom: 1px solid var(--color-border);
    padding: 20px 24px;
    background: #F8FAFC;
  }

  .settings-header {
    display: flex;
    flex-direction: column;
    
    .title {
      font-size: 16px;
      font-weight: 600;
      color: var(--color-text);
      font-family: var(--font-heading);
    }
    
    .subtitle {
      font-size: 13px;
      color: var(--color-text-light);
      margin-top: 4px;
      font-family: var(--font-body);
    }
  }

  .settings-form {
    max-width: 600px;
    padding-top: 10px;
  }
  
// Detail dialog styles (if any, keeping consistent)
:deep(.el-dialog) {
    border-radius: var(--radius-card);
}

  .pwd-alert {
    margin-bottom: 24px;
  }
}
</style>
